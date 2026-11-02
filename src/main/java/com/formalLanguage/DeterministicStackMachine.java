package com.formalLanguage;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Simulator for a deterministic stack machine (DSM).
// State transition table is loaded from a file; simulate() runs the machine on an input word.
public class DeterministicStackMachine {

  private static final String ERROR_STATE = "E";
  private static final char EMPTY_STACK = '#';
  private static final char EPSILON = '~';

  // Key into the transition table: (stateName, stackTop, inputChar)
  private record TransitionKey(String state, char stackTop, char input) {}

  // Value: next state and word to push onto stack (first char becomes top)
  private record TransitionValue(String nextState, String pushWord) {}

  private final Map<TransitionKey, TransitionValue> m_transitions = new HashMap<>();
  private final Set<String> m_acceptingStates = new HashSet<>();
  private String m_startState;

  // Loads and parses the state transition table from stateFilePath
  public DeterministicStackMachine(String stateFilePath) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(stateFilePath));
    parseStateTable(lines);
  }

  // Parses all non-empty lines; the first line defines the start state
  private void parseStateTable(List<String> lines) {
    boolean isFirst = true;
    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty()) continue;
      parseStateLine(line, isFirst);
      isFirst = false;
    }
  }

  // Parses one line: <stateName>: (stackChar,inputChar:newState,newStackWord)* [accepting]
  private void parseStateLine(String line, boolean isFirst) {
    int colonIdx = line.indexOf(':');
    String stateName = line.substring(0, colonIdx).trim();
    String rest = line.substring(colonIdx + 1).trim();

    if (isFirst) {
      m_startState = stateName;
    }

    // Check for accepting keyword at end of line
    if (rest.endsWith("accepting")) {
      rest = rest.substring(0, rest.length() - "accepting".length()).trim();
      m_acceptingStates.add(stateName);
    }

    parseTransitions(stateName, rest);
  }

  // Parses zero or more (stackChar,inputChar:newState,newStackWord) tuples
  private void parseTransitions(String stateName, String text) {
    int pos = 0;
    while (pos < text.length()) {
      if (text.charAt(pos) != '(') { pos++; continue; }
      int close = text.indexOf(')', pos);
      String inner = text.substring(pos + 1, close);
      // Format: stackChar,inputChar:newState,newStackWord
      char stackTop = inner.charAt(0);
      char inputChar = inner.charAt(2);
      int colon = inner.indexOf(':');
      String afterColon = inner.substring(colon + 1);
      int comma = afterColon.indexOf(',');
      String nextState = afterColon.substring(0, comma);
      String pushWord = afterColon.substring(comma + 1);
      m_transitions.put(new TransitionKey(stateName, stackTop, inputChar),
          new TransitionValue(nextState, pushWord));
      pos = close + 1;
    }
  }

  // Simulates the DSM on inputWord, printing each step to System.out
  public void simulate(String inputWord) {
    simulate(inputWord, System.out);
  }

  // Simulates the DSM on inputWord, printing each step to out
  public void simulate(String inputWord, PrintStream out) {
    String currentState = m_startState;
    Deque<Character> stack = initStack();
    int inputPos = 0;

    // Loop while input remains or stack still holds real content (top != #)
    while (inputPos < inputWord.length() || stack.peek() != EMPTY_STACK) {
      char stackTop = stack.peek();
      char nextInput = inputPos < inputWord.length() ? inputWord.charAt(inputPos) : 0;

      // Print before applying transition
      printStep(inputWord.substring(inputPos), currentState, stack, out);

      // Prefer epsilon transition over consuming-input transition
      TransitionValue epsilonTrans = m_transitions.get(
          new TransitionKey(currentState, stackTop, EPSILON));
      if (epsilonTrans != null) {
        currentState = applyTransition(epsilonTrans, stack);
      } else {
        TransitionValue inputTrans = (inputPos < inputWord.length())
            ? m_transitions.get(new TransitionKey(currentState, stackTop, nextInput))
            : null;
        if (inputTrans == null) { currentState = ERROR_STATE; break; }
        currentState = applyTransition(inputTrans, stack);
        inputPos++;
      }

      if (ERROR_STATE.equals(currentState)) break;
    }

    // Print final state after loop
    printStep(inputWord.substring(inputPos), currentState, stack, out);

    printResult(currentState, out);
  }

  // Initialises the stack with the bottom-of-stack marker
  private Deque<Character> initStack() {
    Deque<Character> stack = new ArrayDeque<>();
    stack.push(EMPTY_STACK);
    return stack;
  }

  // Pops stack top, pushes pushWord in reverse so first char becomes new top; returns nextState
  private String applyTransition(TransitionValue trans, Deque<Character> stack) {
    if (!stack.isEmpty()) stack.pop();
    // Push in reverse order so trans.pushWord().charAt(0) ends up on top
    for (int i = trans.pushWord().length() - 1; i >= 0; i--) {
      stack.push(trans.pushWord().charAt(i));
    }
    return trans.nextState();
  }

  // Prints remaining input, current state, and stack (top on left)
  private void printStep(String remainingInput, String state, Deque<Character> stack,
      PrintStream out) {
    StringBuilder sb = new StringBuilder();
    for (char c : stack) sb.append(c);
    out.println(remainingInput + " " + state + " " + sb);
  }

  // Prints ACCEPTED or REJECTED
  private void printResult(String stateName, PrintStream out) {
    if (m_acceptingStates.contains(stateName)) {
      out.println("ACCEPTED");
    } else {
      out.println("REJECTED");
    }
  }
}
