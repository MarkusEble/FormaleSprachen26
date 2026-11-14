package com.formalLanguage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Scanner;

// Performs top-down (predictive) syntax analysis on an input word using a given grammar.
// Uses a stack machine with states Z0 (start), Z (working), Z+ (accept), Z- (error).
public class SyntaxTopDown {

  // Stack machine states
  public enum State { Z0, Z, ACCEPT, ERROR }

  private final Grammar m_grammar;
  private final Scanner m_scanner;

  public SyntaxTopDown(Grammar grammar, Scanner scanner) {
    m_grammar = grammar;
    m_scanner = scanner;
  }

  // Runs top-down syntax analysis on inputWord; returns true if accepted
  public boolean analyze(String inputWord) {
    printTableHeader();

    // Start step: output initial row, move to Z, push start symbol
    State state = State.Z0;
    Deque<Character> stack = new ArrayDeque<>();
    printTableRow(inputWord, state, stack, "");
    state = State.Z;
    stack.push(m_grammar.getStartSymbol());

    // Working loop
    String remaining = inputWord;
    while (state == State.Z) {
      printTableRow(remaining, state, stack, "");
      if (stack.isEmpty()) {
        state = remaining.isEmpty() ? State.ACCEPT : State.ERROR;
        break;
      }
      char top = stack.peek();
      if (isTerminal(top)) {
        remaining = processTerminal(stack, remaining);
        if (remaining == null) {
          state = State.ERROR;
        }
      } else {
        // Non-terminal on top: ask user to select a production
        String appliedProduction = applyProduction(stack, top);
        if (appliedProduction == null) {
          state = State.ERROR;
        } else {
          // Replace last printed row's production column by re-printing with production info
          printAppliedProduction(remaining, state, stack, appliedProduction);
        }
      }
    }

    printResult(state);
    return state == State.ACCEPT;
  }

  // Returns true if the symbol is a terminal in the grammar
  private boolean isTerminal(char symbol) {
    return m_grammar.getTerminals().contains(symbol);
  }

  // Matches terminal on top of stack with head of remaining input; returns new remaining or null on mismatch
  private String processTerminal(Deque<Character> stack, String remaining) {
    char top = stack.pop();
    if (!remaining.isEmpty() && remaining.charAt(0) == top) {
      return remaining.substring(1);
    }
    System.out.println("  Mismatch: expected '" + top + "' but got '" +
        (remaining.isEmpty() ? "<empty>" : String.valueOf(remaining.charAt(0))) + "'");
    return null;
  }

  // Displays available productions for the non-terminal on top, asks user to pick one,
  // replaces top of stack with chosen RHS in reverse order; returns description or null on error
  private String applyProduction(Deque<Character> stack, char nonTerminal) {
    List<String> rhsList = m_grammar.getProductions().get(nonTerminal);
    if (rhsList == null || rhsList.isEmpty()) {
      System.out.println("  Error: no productions for '" + nonTerminal + "'");
      return null;
    }

    // Display available productions
    System.out.println("  Productions for '" + nonTerminal + "':");
    for (int i = 0; i < rhsList.size(); i++) {
      String rhs = rhsList.get(i);
      System.out.println("    [" + (i + 1) + "] " + nonTerminal + " : " + (rhs.isEmpty() ? "ε" : rhs));
    }

    // Read user selection
    int choice = readUserChoice(rhsList.size());
    if (choice < 0) return null;

    String selectedRhs = rhsList.get(choice);
    stack.pop();

    // Push RHS in reverse order so leftmost symbol is on top
    for (int i = selectedRhs.length() - 1; i >= 0; i--) {
      stack.push(selectedRhs.charAt(i));
    }

    return nonTerminal + " : " + (selectedRhs.isEmpty() ? "ε" : selectedRhs);
  }

  // Prompts the user to enter a valid choice between 1 and max; returns 0-based index or -1 on error
  private int readUserChoice(int max) {
    System.out.print("  Select production (1-" + max + "): ");
    if (!m_scanner.hasNextLine()) {
      System.out.println("  Error: no input available");
      return -1;
    }
    String line = m_scanner.nextLine().trim();
    try {
      int choice = Integer.parseInt(line);
      if (choice >= 1 && choice <= max) {
        return choice - 1;
      }
      System.out.println("  Error: choice must be between 1 and " + max);
    } catch (NumberFormatException e) {
      System.out.println("  Error: invalid input '" + line + "'");
    }
    return -1;
  }

  // Prints the column headers for the analysis table
  private void printTableHeader() {
    System.out.printf("%-25s %-10s %-25s %s%n",
        "Remaining Input Word", "State", "Stack", "Applied Production");
    System.out.println("-".repeat(80));
  }

  // Prints a single table row (without production; used for initial and terminal steps)
  private void printTableRow(String remaining, State state, Deque<Character> stack, String production) {
    System.out.printf("%-25s %-10s %-25s %s%n",
        remaining.isEmpty() ? "ε" : remaining,
        stateLabel(state),
        stackString(stack),
        production);
  }

  // Prints a row after a production was applied (overwrites current row placeholder)
  private void printAppliedProduction(String remaining, State state, Deque<Character> stack, String production) {
    System.out.printf("  -> Applied: %s%n", production);
  }

  // Converts state enum to display label
  private String stateLabel(State state) {
    return switch (state) {
      case Z0 -> "Z0";
      case Z -> "Z";
      case ACCEPT -> "Z+";
      case ERROR -> "Z-";
    };
  }

  // Converts the stack contents to a string (top first)
  private String stackString(Deque<Character> stack) {
    if (stack.isEmpty()) return "ε";
    StringBuilder sb = new StringBuilder();
    for (char c : stack) {
      sb.append(c);
    }
    return sb.toString();
  }

  // Prints the final accept/reject result
  private void printResult(State state) {
    System.out.println("-".repeat(80));
    if (state == State.ACCEPT) {
      System.out.println("Result: ACCEPTED (Z+) - word is in the language");
    } else {
      System.out.println("Result: REJECTED (Z-) - word is not in the language");
    }
  }
}
