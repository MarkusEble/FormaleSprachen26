package com.formalLanguage;

import java.io.IOException;
import java.util.Map;

// Entry point: reads a grammar file given as a CLI argument and checks it for consistency
public class GrammarMain {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: GrammarMain <grammar-file>");
      System.exit(1);
    }

    String filePath = args[0];

    // Load grammar, run consistency check, determine grammar type, and check CNF
    try {
      Grammar grammar = new Grammar(filePath);
      grammar.checkConsistency();
      System.out.println("Grammar is regular: " + grammar.isRegular());
      printChomskyNormalFormResults(grammar);
    } catch (IOException e) {
      System.err.println("Error: could not read grammar file '" + filePath + "': " + e.getMessage());
      System.exit(1);
    }
  }

  // Prints each production rule and whether it is in Chomsky Normal Form
  private static void printChomskyNormalFormResults(Grammar grammar) {
    System.out.println("Chomsky Normal Form check:");
    for (Map.Entry<String, Boolean> entry : grammar.checkChomskyNormalForm().entrySet()) {
      System.out.println("  " + entry.getKey() + "  ->  CNF: " + entry.getValue());
    }
  }
}
