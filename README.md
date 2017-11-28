# Ott-IFC
Developping sound information-flow control mechanisms can be a laborious and error-prone task due to the numerous ways through which information may flow in a program, particularly when dealing with complex programming languages. Ott-IFC seeks to help with this task, by generating basic information-flow control mechanisms from language specifications (i.e., syntax and semantics). 

As the name implies, the specifications that Ott-IFC takes as input (and outputs) are written in [Ott](https://github.com/ott-lang/ott). Ott is a tool that can generate LaTeX, Coq or Isabelle/HOL versions of a programming language's specification. The specification is written in a concise and readable ASCII notation that resembles what one would write in informal mathematics.

## Usage
```
-i [.ott file]                       Specification to use as input.
-m [generation | verification]       Ott-IFC's mode. Use "generation" to generate a mechanism and "verification" to verify an existing mechanism.
```

### Assumptions
 - The specification for the language is contained in a single file
 - The language's syntax is composed of two types of productions: expressions and commands.