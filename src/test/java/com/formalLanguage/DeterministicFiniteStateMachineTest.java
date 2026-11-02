package com.formalLanguage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DeterministicFiniteStateMachineTest {

  @TempDir
  Path tempDir;

  // Writes state file content to a temp file and returns its path
  private Path writeStateFile(String content) throws IOException {
    Path file = tempDir.resolve("states.txt");
    Files.writeString(file, content);
    return file;
  }

  // Runs simulation and returns output as a normalized (LF-only) string
  private String simulate(String stateContent, String inputWord) throws IOException {
    Path file = writeStateFile(stateContent);
    DeterministicFiniteStateMachine machine = new DeterministicFiniteStateMachine(file.toString());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    machine.simulate(inputWord, new PrintStream(baos));
    return baos.toString().replace("\r\n", "\n");
  }

  // Accepted word prints each transition step followed by ACCEPTED
  @Test
  void simulate_acceptedWord_printsTransitionsAndAccepted() throws IOException {
    String result = simulate("q0: (a:q1)\nq1: accepting", "a");
    assertEquals("q0 'a' -> q1\nACCEPTED\n", result);
  }

  // Word that exhausts input in a non-accepting state prints REJECTED
  @Test
  void simulate_wordEndsInNonAcceptingState_printsRejected() throws IOException {
    String result = simulate("q0: (a:q1)\nq1:", "a");
    assertTrue(result.contains("q0 'a' -> q1"));
    assertTrue(result.endsWith("REJECTED\n"));
  }

  // Unlisted transition leads to implicit ErrorState E and stops processing
  @Test
  void simulate_unlistedTransition_transitionsToErrorState() throws IOException {
    String result = simulate("q0: (a:q1)\nq1: accepting", "b");
    assertTrue(result.contains("q0 'b' -> E"));
    assertTrue(result.endsWith("REJECTED\n"));
  }

  // Processing stops immediately on reaching E (no further steps output)
  @Test
  void simulate_errorStateBreaksLoop_noFurtherSteps() throws IOException {
    String result = simulate("q0: (a:q1)\nq1: accepting", "ba");
    long stepCount = result.lines().filter(l -> l.contains("->")).count();
    assertEquals(1, stepCount);
  }

  // First line of file defines the start state
  @Test
  void simulate_firstLineIsStartState_beginsFromFirstState() throws IOException {
    String result = simulate("q0: (a:q1)\nq1: (a:q1) accepting", "aa");
    assertTrue(result.startsWith("q0"));
  }

  // Multi-step word: all transitions are printed before the result
  @Test
  void simulate_multiStepWord_allTransitionsPrinted() throws IOException {
    String result = simulate("q0: (a:q1)\nq1: (b:q2)\nq2: accepting", "ab");
    assertTrue(result.contains("q0 'a' -> q1"));
    assertTrue(result.contains("q1 'b' -> q2"));
    assertTrue(result.endsWith("ACCEPTED\n"));
  }

  // Empty input word: ACCEPTED when start state is accepting
  @Test
  void simulate_emptyInput_acceptingStartState_returnsAccepted() throws IOException {
    String result = simulate("q0: accepting", "");
    assertEquals("ACCEPTED\n", result);
  }

  // Empty input word: REJECTED when start state is not accepting
  @Test
  void simulate_emptyInput_nonAcceptingStartState_returnsRejected() throws IOException {
    String result = simulate("q0: (a:q1)\nq1: accepting", "");
    assertEquals("REJECTED\n", result);
  }

  // Constructor throws IOException for a missing file
  @Test
  void constructor_missingFile_throwsIOException() {
    assertThrows(IOException.class, () ->
        new DeterministicFiniteStateMachine(tempDir.resolve("nonexistent.txt").toString()));
  }

  // Self-loop machine: accepts strings of zero or more 'a' followed by one 'b'
  @Test
  void simulate_selfLoopMachine_acceptsCorrectWords() throws IOException {
    String states = "q0: (a:q0)(b:q1)\nq1: accepting";
    assertTrue(simulate(states, "b").endsWith("ACCEPTED\n"));
    assertTrue(simulate(states, "aaab").endsWith("ACCEPTED\n"));
    assertTrue(simulate(states, "a").endsWith("REJECTED\n"));
    assertTrue(simulate(states, "bb").endsWith("REJECTED\n"));
  }
}
