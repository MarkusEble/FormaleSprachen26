# Purpose
Create a java class using top down syntax analysis to determine if a word can be created by a given grammar
based on the grammar class described in Grammar.md

# Style
Strictly adhere to style.md

# Stack machine
Top down syntax analysis uses a stack machine.
The stack machine has states Z0 (= start state), Z (= working state), (Z+ = accepting state), (Z- = error state)
The stack machine has a stack. Stack entries can be any terminal and non terminal of the grammar.

# Top down syntax analysis
Output is done into a table with column headers (Remaining Input Word, State, Stack, Applied production)

## Start step
Output column headers
RemainingInputWord = word to check
Stack is empty
State = Z0
Output RemainingInputWord, State, Stack in new table row
Move from state Z0 to state Z
Push the start symbol of the grammar on top of stack

## Repeated working step

Output RemainingInputWord, State, Stack

if a terminal symbol is on top of stack in new table row
  compare with current head of input word
  if match
    consume terminal symbol from stack and from RemainingInputWord
  else
    switch to state Z-
    break loop
else if a non terminal symbol is on top of stack
  display all available productions for this non terminal symbol and ask the user to select one
  replace the non terminal symbol with the right side of the selected production in reverse order so that the leftmost symbol of the right side is on top of stack
  Output the applied production in the current table row
else // stack is empty
  if input word is empty
    switch to state Z+
  else
    switch to state Z-
  break loop

## Finish
Report if processing ended in state Z+ accept or Z- error

# Example
Create an example grammar file
Create a main program that takes a grammar file path and an input word as command line input argument and applies top down syntax analysis
Create a launch configuration