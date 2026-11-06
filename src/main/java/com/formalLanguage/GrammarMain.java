package com.formalLanguage;

import java.io.IOException;

// Entry point: reads a grammar file given as a CLI argument and checks it for consistency
public class GrammarMain {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: GrammarMain <grammar-file>");
      System.exit(1);
    }

    String filePath = args[0];

    // Load grammar and run consistency check
    try {
      Grammar grammar = new Grammar(filePath);
      grammar.checkConsistency();
    } catch (IOException e) {
      System.err.println("Error: could not read grammar file '" + filePath + "': " + e.getMessage());
      System.exit(1);
    }
  }
}
