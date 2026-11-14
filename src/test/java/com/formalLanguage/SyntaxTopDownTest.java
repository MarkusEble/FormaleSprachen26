package com.formalLanguage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class SyntaxTopDownTest {

  @TempDir
  Path tempDir;

  // Writes grammar content to a temp file and returns its path string
  private String writeGrammar(String content) throws IOException {
    Path file = tempDir.resolve("grammar.txt");
    Files.writeString(file, content);
    return file.toString();
  }

  // Creates an analyzer backed by a string of newline-separated choices
  private SyntaxTopDown makeAnalyzer(Grammar grammar, String choices) {
    return new SyntaxTopDown(grammar, new Scanner(choices));
  }

  // Grammar: S : aSb | ε  -> accepts a^n b^n
  // "aabb": select S->aSb (1), then S->aSb (1), then S->ε (2)
  @Test
  void analyze_anbn_acceptsAabb() throws IOException {
    String path = writeGrammar("N = { S }\nT = { a, b }\nS\nS : aSb\nS :\n");
    Grammar grammar = new Grammar(path);
    // Choices: production 1 (aSb), production 1 (aSb), production 2 (ε)
    SyntaxTopDown analyzer = makeAnalyzer(grammar, "1\n1\n2\n");
    assertTrue(analyzer.analyze("aabb"));
  }

  // Empty word with S->ε production is accepted
  @Test
  void analyze_emptyWord_withEpsilonProduction_accepts() throws IOException {
    String path = writeGrammar("N = { S }\nT = { a }\nS\nS : a\nS :\n");
    Grammar grammar = new Grammar(path);
    // Choose production 2 (ε) for empty word
    SyntaxTopDown analyzer = makeAnalyzer(grammar, "2\n");
    assertTrue(analyzer.analyze(""));
  }

  // Single terminal matches: S : a, input "a"
  @Test
  void analyze_singleTerminal_accepts() throws IOException {
    String path = writeGrammar("N = { S }\nT = { a }\nS\nS : a\n");
    Grammar grammar = new Grammar(path);
    // Select only production S->a
    SyntaxTopDown analyzer = makeAnalyzer(grammar, "1\n");
    assertTrue(analyzer.analyze("a"));
  }

  // Wrong terminal causes rejection: S : a, input "b"
  @Test
  void analyze_terminalMismatch_rejects() throws IOException {
    String path = writeGrammar("N = { S }\nT = { a, b }\nS\nS : a\nS : b\n");
    Grammar grammar = new Grammar(path);
    // Select S->a but input is "b"
    SyntaxTopDown analyzer = makeAnalyzer(grammar, "1\n");
    assertFalse(analyzer.analyze("b"));
  }

  // Input longer than grammar produces causes rejection
  @Test
  void analyze_inputTooLong_rejects() throws IOException {
    String path = writeGrammar("N = { S }\nT = { a }\nS\nS : a\n");
    Grammar grammar = new Grammar(path);
    // Select S->a, stack becomes empty after consuming 'a', but "aa" still has 'a' remaining
    SyntaxTopDown analyzer = makeAnalyzer(grammar, "1\n");
    assertFalse(analyzer.analyze("aa"));
  }

  // Stack empty but input remaining causes Z- state
  @Test
  void analyze_stackEmptyInputRemaining_rejects() throws IOException {
    String path = writeGrammar("N = { S }\nT = { a }\nS\nS :\n");
    Grammar grammar = new Grammar(path);
    // Select S->ε (empty production), then stack is empty but input "a" remains
    SyntaxTopDown analyzer = makeAnalyzer(grammar, "1\n");
    assertFalse(analyzer.analyze("a"));
  }
}
