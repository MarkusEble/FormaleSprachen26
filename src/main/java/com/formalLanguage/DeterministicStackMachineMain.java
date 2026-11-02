package com.formalLanguage;

// Entry point for the DSM simulator.
// Usage: --stateFile=<file> --inputWord=<word>
public class DeterministicStackMachineMain {

  public static void main(String[] args) {
    String stateFile = null;
    String inputWord = null;

    // Parse --stateFile and --inputWord from command line arguments
    for (String arg : args) {
      if (arg.startsWith("--stateFile=")) {
        stateFile = arg.substring("--stateFile=".length());
      } else if (arg.startsWith("--inputWord=")) {
        inputWord = arg.substring("--inputWord=".length());
      }
    }

    if (stateFile == null || inputWord == null) {
      System.err.println("Usage: --stateFile=<file> --inputWord=<word>");
      System.exit(1);
    }

    try {
      DeterministicStackMachine machine = new DeterministicStackMachine(stateFile);
      machine.simulate(inputWord);
    } catch (Exception e) {
      System.err.println("Error loading state file '" + stateFile + "': " + e.getMessage());
      System.exit(1);
    }
  }
}
