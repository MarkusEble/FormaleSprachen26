# Purpose
Create a java class that reads the definition of a formal language grammar and later can do operations/checks on the grammar

# Style
Strictly adhere to style.md

# Grammar specification
A grammar is a tuple with 4 components:
- a set of non terminal symbols
- a set of terminal symbols
- one of the non terminal symbols is the start symbol
- a set of production rules, each mapping a non-terminal symbol to sequence of ternmil and non terminal symbols that might be empty

For simplifcation we define that non terminal symbols are always a upper case letter
Non terminal symbols are alwazs a lower case letter or a number

# File format to represent a grammar
Extension shall be .txt
First line of file denotes the set of non terminal symbols as N = { comma separated list of non terminal symbols}
Second line of file denotes the set of terminal symbols as T = { comma separated list of terminal symbols }
Third line denotes the start symbol
All following lines contain productions in the form 
<NonTerminalSymbol> : <Terminal or NonTerminalSymbol>*

# Operations
Create a method that takes the name of a grammar file and reads it
Create a method to do a consistency check
- check if all terminal and non terminal symbols used in the productions are part of the sets N or T
- for each non terminal symbol in N there must be a production rule with the non terminal symbol on the left side and not on the right side

# Example
Create an example grammar file
Create a main program that takes a grammar file as command line input argument and checks it for consistency