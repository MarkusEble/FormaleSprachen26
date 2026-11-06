package com.formalLanguage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Represents a formal grammar G = (N, T, S, P) loaded from a text file.
// Provides a consistency check to validate the grammar rules.
public class Grammar {

  // Non-terminal symbols (uppercase letters)
  private final Set<Character> m_nonTerminals = new HashSet<>();
  // Terminal symbols (lowercase letters or digits)
  private final Set<Character> m_terminals = new HashSet<>();
  // Start symbol (one of the non-terminals)
  private char m_startSymbol;
  // Productions: non-terminal -> list of right-hand side strings
  private final Map<Character, List<String>> m_productions = new HashMap<>();

  // Reads and parses a grammar file from grammarFilePath
  public Grammar(String grammarFilePath) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(grammarFilePath));
    if (lines.size() < 3) {
      System.err.println("Error: grammar file must have at least 3 lines (N, T, start symbol): " + grammarFilePath);
      System.exit(1);
    }
    parseNonTerminals(lines.get(0));
    parseTerminals(lines.get(1));
    parseStartSymbol(lines.get(2));
    parseProductions(lines.subList(3, lines.size()));
  }

  // Parses the non-terminal set from a line like "N = { A, B, C }"
  private void parseNonTerminals(String line) {
    for (char c : extractSymbols(line)) {
      m_nonTerminals.add(c);
    }
  }

  // Parses the terminal set from a line like "T = { a, b, 0 }"
  private void parseTerminals(String line) {
    for (char c : extractSymbols(line)) {
      m_terminals.add(c);
    }
  }

  // Extracts comma-separated single characters from inside the curly braces
  private List<Character> extractSymbols(String line) {
    List<Character> result = new ArrayList<>();
    int open = line.indexOf('{');
    int close = line.indexOf('}');
    if (open < 0 || close < 0) return result;
    String inner = line.substring(open + 1, close);
    for (String token : inner.split(",")) {
      String trimmed = token.trim();
      if (trimmed.length() == 1) {
        result.add(trimmed.charAt(0));
      }
    }
    return result;
  }

  // Parses the start symbol from the third line (single character)
  private void parseStartSymbol(String line) {
    String trimmed = line.trim();
    if (trimmed.isEmpty()) {
      System.err.println("Error: start symbol line is empty");
      System.exit(1);
    }
    m_startSymbol = trimmed.charAt(0);
  }

  // Parses production lines of the form "<NonTerminal> : <rhs>"
  private void parseProductions(List<String> lines) {
    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty()) continue;
      int colonIdx = line.indexOf(':');
      if (colonIdx < 0) {
        System.err.println("Error: invalid production line (missing ':'): " + line);
        System.exit(1);
      }
      char lhs = line.substring(0, colonIdx).trim().charAt(0);
      // Right-hand side may be empty (epsilon production)
      String rhs = line.substring(colonIdx + 1).trim();
      m_productions.computeIfAbsent(lhs, k -> new ArrayList<>()).add(rhs);
    }
  }

  // Checks grammar consistency; prints all errors and returns true if consistent
  public boolean checkConsistency() {
    List<String> errors = new ArrayList<>();
    checkProductionSymbols(errors);
    checkNonTerminalCoverage(errors);

    if (errors.isEmpty()) {
      System.out.println("Grammar is consistent.");
      return true;
    }
    for (String error : errors) {
      System.out.println("Inconsistency: " + error);
    }
    return false;
  }

  // Checks that every symbol in every production is in N or T
  private void checkProductionSymbols(List<String> errors) {
    for (Map.Entry<Character, List<String>> entry : m_productions.entrySet()) {
      char lhs = entry.getKey();
      if (!m_nonTerminals.contains(lhs)) {
        errors.add("Production LHS '" + lhs + "' is not in N");
      }
      for (String rhs : entry.getValue()) {
        checkRhsSymbols(lhs, rhs, errors);
      }
    }
  }

  // Checks that each character in a RHS string belongs to N or T
  private void checkRhsSymbols(char lhs, String rhs, List<String> errors) {
    for (char c : rhs.toCharArray()) {
      if (!m_nonTerminals.contains(c) && !m_terminals.contains(c)) {
        errors.add("Symbol '" + c + "' in production '" + lhs + " : " + rhs + "' is not in N or T");
      }
    }
  }

  // Checks that each non-terminal has at least one production where it appears only on the LHS
  private void checkNonTerminalCoverage(List<String> errors) {
    for (char nt : m_nonTerminals) {
      if (!m_productions.containsKey(nt)) {
        errors.add("Non-terminal '" + nt + "' has no production rule");
        continue;
      }
      boolean hasLhsOnlyRule = hasProductionWithLhsNotInRhs(nt);
      if (!hasLhsOnlyRule) {
        errors.add("Non-terminal '" + nt + "' has no production where it appears only on the LHS (not in any RHS)");
      }
    }
  }

  // Returns true if nt has at least one production where nt does not appear in the RHS
  private boolean hasProductionWithLhsNotInRhs(char nt) {
    for (String rhs : m_productions.get(nt)) {
      if (rhs.indexOf(nt) < 0) {
        return true;
      }
    }
    return false;
  }

  public Set<Character> getNonTerminals() { return m_nonTerminals; }
  public Set<Character> getTerminals() { return m_terminals; }
  public char getStartSymbol() { return m_startSymbol; }
  public Map<Character, List<String>> getProductions() { return m_productions; }
}
