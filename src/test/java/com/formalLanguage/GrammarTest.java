package com.formalLanguage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GrammarTest {

  @TempDir
  Path tempDir;

  // Writes grammar content to a temp file and returns its path
  private Path writeGrammar(String content) throws IOException {
    Path file = tempDir.resolve("grammar.txt");
    Files.writeString(file, content);
    return file;
  }

  // Valid grammar parses all 4 components correctly
  @Test
  void constructor_validGrammar_parsesAllComponents() throws IOException {
    Path file = writeGrammar("N = { S, A }\nT = { a, b }\nS\nS : aA\nA : b\n");
    Grammar g = new Grammar(file.toString());
    assertTrue(g.getNonTerminals().contains('S'));
    assertTrue(g.getNonTerminals().contains('A'));
    assertTrue(g.getTerminals().contains('a'));
    assertTrue(g.getTerminals().contains('b'));
    assertEquals('S', g.getStartSymbol());
    assertTrue(g.getProductions().containsKey('S'));
    assertTrue(g.getProductions().containsKey('A'));
  }

  // Constructor throws IOException for a missing file
  @Test
  void constructor_missingFile_throwsIOException() {
    assertThrows(IOException.class, () ->
        new Grammar(tempDir.resolve("nonexistent.txt").toString()));
  }

  // Consistent grammar returns true from checkConsistency
  @Test
  void checkConsistency_validGrammar_returnsTrue() throws IOException {
    Path file = writeGrammar("N = { S, A }\nT = { a, b }\nS\nS : aA\nA : b\n");
    Grammar g = new Grammar(file.toString());
    assertTrue(g.checkConsistency());
  }

  // Grammar with unknown symbol in RHS is inconsistent
  @Test
  void checkConsistency_unknownRhsSymbol_returnsFalse() throws IOException {
    // 'c' is not in N or T
    Path file = writeGrammar("N = { S }\nT = { a }\nS\nS : ac\n");
    Grammar g = new Grammar(file.toString());
    assertFalse(g.checkConsistency());
  }

  // Grammar where a non-terminal only appears in RHS (no terminating rule) is inconsistent
  @Test
  void checkConsistency_nonTerminalOnlyInRhs_returnsFalse() throws IOException {
    // A only has a self-referential production
    Path file = writeGrammar("N = { S, A }\nT = { a }\nS\nS : aA\nA : aA\n");
    Grammar g = new Grammar(file.toString());
    assertFalse(g.checkConsistency());
  }

  // Non-terminal without any production rule is inconsistent
  @Test
  void checkConsistency_nonTerminalWithNoProduction_returnsFalse() throws IOException {
    // B is declared but has no production
    Path file = writeGrammar("N = { S, B }\nT = { a }\nS\nS : a\n");
    Grammar g = new Grammar(file.toString());
    assertFalse(g.checkConsistency());
  }

  // Epsilon production (empty RHS) is valid and consistent
  @Test
  void checkConsistency_epsilonProduction_returnsTrue() throws IOException {
    Path file = writeGrammar("N = { S }\nT = { a }\nS\nS : a\nS : \n");
    Grammar g = new Grammar(file.toString());
    assertTrue(g.checkConsistency());
  }

  // Non-terminal with both a self-referential and a terminating rule is consistent
  @Test
  void checkConsistency_nonTerminalWithSelfLoopAndTerminating_returnsTrue() throws IOException {
    Path file = writeGrammar("N = { S }\nT = { a }\nS\nS : aS\nS : a\n");
    Grammar g = new Grammar(file.toString());
    assertTrue(g.checkConsistency());
  }

  // LHS symbol not in N is inconsistent
  @Test
  void checkConsistency_lhsSymbolNotInN_returnsFalse() throws IOException {
    // 'X' appears as LHS but is not in N
    Path file = writeGrammar("N = { S }\nT = { a }\nS\nS : a\nX : a\n");
    Grammar g = new Grammar(file.toString());
    assertFalse(g.checkConsistency());
  }
}
