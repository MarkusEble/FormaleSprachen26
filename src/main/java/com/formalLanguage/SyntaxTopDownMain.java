package com.formalLanguage;

import java.io.IOException;
import java.util.Scanner;

// Entry point: loads a grammar file and runs top-down syntax analysis on an input word
public class SyntaxTopDownMain {

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: SyntaxTopDownMain <grammar-file> <input-word>");
      System.exit(1);
    }

    String grammarFile = args[0];
    String inputWord = args[1];

    try {
      Grammar grammar = new Grammar(grammarFile);
      if (!grammar.checkConsistency()) {
        System.err.println("Error: grammar is inconsistent, aborting analysis");
        System.exit(1);
      }

      // Use stdin for interactive production selection
      Scanner scanner = new Scanner(System.in);
      SyntaxTopDown analyzer = new SyntaxTopDown(grammar, scanner);
      analyzer.analyze(inputWord);
    } catch (IOException e) {
      System.err.println("Error: could not read grammar file '" + grammarFile + "': " + e.getMessage());
      System.exit(1);
    }
  }
}
