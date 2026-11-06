# indent
- 2 spaces
- opening brace of a code block in the same line as the control statement it belongs to
- closing brace of a code block on a separate line

# naming
- camel case
- constants and class names start with an uppercase character
- method and variable names start with an lowercase character
- member names start with m_

# comments
- comment purpose
- comment logical steps
- use short concise comments
- as a rule of thumb have a comment every 5-10 lines
- insert comments describing implementation in complex cases

# code structure
- small methods focussed on one purpose and named accordingly
- methods should typically be < 20 lines of code and shall not exceed 50 lines of code

# robustness
- check all accesses to files for errors
- in case of errors output an error message describing the action that failed and the current step of processing and end execution

# test
- always create automated tests