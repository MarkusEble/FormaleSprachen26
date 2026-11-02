package com.formalLanguage;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Simulator for a deterministic finite state machine (DFSM).
// State transition table is loaded from a file; simulate() runs the machine on an input word.
public class DeterministicFiniteStateMachine {

  private static final String ERROR_STATE = "E";

  // State name -> (input character -> next state name)
  private final Map<String, Map<Character, String>> m_transitions = new HashMap<>();
  private final Set<String> m_acceptingStates = new HashSet<>();
  private String m_startState;

  // Loads and parses the state transition table from stateFilePath
  public DeterministicFiniteStateMachine(String stateFilePath) throws IOException {
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

  // Parses one line: <stateName>: (char:newState)* [accepting]
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

    m_transitions.put(stateName, parseTransitions(rest));
  }

  // Parses zero or more (char:newState) pairs from the given string
  private Map<Character, String> parseTransitions(String text) {
    Map<Character, String> transitions = new HashMap<>();
    int pos = 0;
    while (pos < text.length()) {
      if (text.charAt(pos) != '(') { pos++; continue; }
      int close = text.indexOf(')', pos);
      String pair = text.substring(pos + 1, close);
      char inputChar = pair.charAt(0);
      // inputChar is always one character; position 1 is the separator (: or ,)
      String targetState = pair.substring(2);
      transitions.put(inputChar, targetState);
      pos = close + 1;
    }
    return transitions;
  }

  // Simulates the DFSM on inputWord, printing each step to System.out
  public void simulate(String inputWord) {
    simulate(inputWord, System.out);
  }

  // Simulates the DFSM on inputWord, printing each step to out
  public void simulate(String inputWord, PrintStream out) {
    String current = m_startState;

    // Process each input character and print the transition
    for (int i = 0; i < inputWord.length(); i++) {
      char c = inputWord.charAt(i);
      String next = getNextState(current, c);
      out.println(current + " '" + c + "' -> " + next);
      current = next;
      if (ERROR_STATE.equals(current)) break;
    }

    printResult(current, out);
  }

  // Returns the next state for currentState on inputChar; unmapped transitions go to E
  private String getNextState(String stateName, char inputChar) {
    Map<Character, String> transitions = m_transitions.get(stateName);
    if (transitions == null) return ERROR_STATE;
    return transitions.getOrDefault(inputChar, ERROR_STATE);
  }

  // Prints ACCEPTED or REJECTED based on whether stateName is an accepting state
  private void printResult(String stateName, PrintStream out) {
    if (m_acceptingStates.contains(stateName)) {
      out.println("ACCEPTED");
    } else {
      out.println("REJECTED");
    }
  }
}
