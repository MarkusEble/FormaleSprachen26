package com.formalLanguage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DeterministicStackMachineTest {

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
    DeterministicStackMachine machine = new DeterministicStackMachine(file.toString());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    machine.simulate(inputWord, new PrintStream(baos));
    return baos.toString().replace("\r\n", "\n");
  }

  // Recognizes a^n b^n: q0 pushes 'A' per 'a', q1 pops 'A' per 'b', q2 accepts on empty stack (#)
  // Language a^n b^n (n >= 1)
  private static final String ANBN =
      "q0: (#,a:q0,A#)(A,a:q0,AA)(A,b:q1,)accepting\n" +
      "q1: (A,b:q1,)(#,~:q2,#)accepting\n";

  // Accepted word "ab" produces step output and ends with ACCEPTED
  @Test
  void simulate_ab_accepted() throws IOException {
    String result = simulate(ANBN, "ab");
    assertTrue(result.endsWith("ACCEPTED\n"), result);
  }

  // Accepted word "aabb" ends with ACCEPTED
  @Test
  void simulate_aabb_accepted() throws IOException {
    String result = simulate(ANBN, "aabb");
    assertTrue(result.endsWith("ACCEPTED\n"), result);
  }

  // "aab" has more a's than b's: should be REJECTED
  @Test
  void simulate_aab_rejected() throws IOException {
    String result = simulate(ANBN, "aab");
    assertTrue(result.endsWith("REJECTED\n"), result);
  }

  // "abb" has more b's than a's: should be REJECTED
  @Test
  void simulate_abb_rejected() throws IOException {
    String result = simulate(ANBN, "abb");
    assertTrue(result.endsWith("REJECTED\n"), result);
  }

  // Unlisted transition (no matching entry) causes the machine to stop and REJECT
  @Test
  void simulate_noMatchingTransition_rejects() throws IOException {
    // Single state, only pushes 'a'; 'b' has no transition
    String states = "q0: (#,a:q0,A#)\nq1: accepting";
    String result = simulate(states, "ab");
    assertTrue(result.endsWith("REJECTED\n"), result);
  }

  // Epsilon transition is taken before consuming input
  @Test
  void simulate_epsilonTransition_takenBeforeInput() throws IOException {
    // q0 has epsilon transition on # -> q1; q1 accepts on 'a'
    String states = "q0: (#,~:q1,#)\nq1: (#,a:q2,#)\nq2: accepting";
    String result = simulate(states, "a");
    assertTrue(result.endsWith("ACCEPTED\n"), result);
    // epsilon step must appear before input-consuming step
    int epsilonLine = result.indexOf("q1");
    int inputLine = result.indexOf("q2");
    assertTrue(epsilonLine < inputLine, result);
  }

  // Stack interaction: pushWord's first char becomes top of stack
  @Test
  void simulate_pushWordOrder_firstCharIsTop() throws IOException {
    // q0 pushes "BA" on # with input 'x', then q1 reads 'B' from stack with epsilon
    // Verifies top is 'B' (first char of "BA")
    String states =
        "q0: (#,x:q1,BA#)\n" +
        "q1: (B,~:q2,)\n" +
        "q2: (A,~:q3,)\n" +
        "q3: accepting";
    String result = simulate(states, "x");
    assertTrue(result.endsWith("ACCEPTED\n"), result);
  }

  // Error state E: once entered, loop breaks and result is REJECTED
  @Test
  void simulate_errorState_stopsAndRejects() throws IOException {
    // Only 'a' is defined; 'b' triggers implicit error
    String states = "q0: (#,a:q1,#)\nq1: accepting";
    String result = simulate(states, "ba");
    assertTrue(result.endsWith("REJECTED\n"), result);
    // Only one step line before REJECTED (the 'b' step hitting E)
    long steps = result.lines().filter(l -> !l.equals("REJECTED")).count();
    assertEquals(2, steps, result);
  }

  // Empty input with accepting start state is ACCEPTED immediately
  @Test
  void simulate_emptyInput_acceptingStartState_accepted() throws IOException {
    // q0 has epsilon that transitions to accepting q1 when stack is #
    String states = "q0: accepting";
    String result = simulate(states, "");
    assertTrue(result.endsWith("ACCEPTED\n"), result);
  }

  // Constructor throws IOException for missing file
  @Test
  void constructor_missingFile_throwsIOException() {
    assertThrows(IOException.class, () ->
        new DeterministicStackMachine(tempDir.resolve("nonexistent.txt").toString()));
  }

  // Each step line prints: remainingInput state stack
  @Test
  void simulate_stepFormat_containsRemainingInputStateStack() throws IOException {
    String states = "q0: (#,a:q1,A#)\nq1: accepting";
    String result = simulate(states, "a");
    // After consuming 'a', remaining input is empty, state is q1, stack top is A
    String[] lines = result.split("\n");
    // First line is the step after consuming 'a'
    assertEquals("a q0 #", lines[0]);
    assertEquals(" q1 A#", lines[1]);
  }
}
