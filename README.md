# Ott-IFC
Developping sound information-flow control mechanisms can be a laborious and error-prone task due to the numerous ways through which information may flow in a program, particularly when dealing with complex programming languages. Ott-IFC seeks to help with this task, by generating basic information-flow control mechanisms from language specifications (i.e., syntax and semantics). 

As the name implies, the specifications that Ott-IFC takes as input (and outputs) are written in [Ott](https://github.com/ott-lang/ott). Ott is a tool that can generate LaTeX, Coq or Isabelle/HOL versions of a programming language's specification. The specification is written in a concise and readable ASCII notation that resembles what one would write in informal mathematics.

##Example
```
%grammar
    arith_expr, a :: ae_ ::=
        | x                                 ::  :: variable
        | n                                 ::  :: int
        | a1 + a2                           ::  :: addition
        | a1 * a2                           ::  :: multiplication
    
    bool_expr, b :: be_ ::=
        | true                              ::  :: true
        | false                             ::  :: false
        | a1 < a2                           ::  :: less_than
    
    commands, cmd :: cmd_ ::=
        | stop                              ::  :: stop
        | skip                              ::  :: skip
        | x := a                            ::  :: assignment
        | cmd1 ; cmd2                       ::  :: sequence
        | if b then cmd1 else cmd2 end      ::  :: if
        | while b do cmd end                ::  :: while
        | read x from ch                    ::  :: read
        | write x to ch                     ::  :: write
```

To prevent explicit flows, Ott-IFC identifies the semantic rules that may modify the memory *m* (e.g., rule `assign`). In each of those rules, it updates the modified variable's label with the label of the expressions that are used in the rule.
```
    <a, m, o> || <n, m, o>
    ----------------------------- :: assign
    <x := a, m, o> || <stop, m[x |-> n], o>

is transformed into

    E |- a : l_a
    <E, pc, a, m, o> || <E, pc, n, m, o>
    ----------------------------- :: assign
    <E, pc, x := a, m, o> ||  <E[x |-> pc |_| l_a], pc, stop, m[x |-> n], o>
```


To prevent implicit flows, it identifies commands that may influence the control-flow of the application. That is, commands for which a program configuration may lead to two different program configurations (e.g., the `if` command). It then updates to the program counter `pc` with the level of the expressions that are present in the rule (only `b` in this case).

```
    <b, m, o> || <true, m, o>
    <cmd1, m, o> || <stop, m1, o1>
    ---------------------------------------------------------------- :: if_true
    <if b then cmd1 else cmd2 end, m, o> || <stop, m1, o1>
    
    <b, m, o> || <false, m, o>
    <cmd2, m, o> || <stop, m2, o2>
    ---------------------------------------------------------------- :: if_false
    <if b then cmd1 else cmd2 end, m, o> || <stop, m2, o2>
    
is transformed into

    E |- b : l_b
    <E, pc, b, m, o> || <E, pc, true, m, o>
    <E, pc |_| l_b, cmd1, m, o> || <E, pc |_| l_b, stop, m1, o1>
    ---------------------------------------------------------------- :: if_true
    <E, pc, if b then cmd1 else cmd2 end, m, o> ||  <E, pc, stop, m1, o1>
    
    E |- b : l_b
    <E, pc |_| l_b, cmd2, m, o> || <E, pc |_| l_b, stop, m2, o2>
    <E, pc, b, m, o> || <E, pc, false, m, o>
    ---------------------------------------------------------------- :: if_false
    <E, pc, if b then cmd1 else cmd2 end, m, o> ||  <E, pc, stop, m2, o2>
```


## Usage
```
-i [.ott file]                       Specification to use as input.
-m [generation | verification]       Ott-IFC's mode. Use "generation" to generate a mechanism and "verification" to verify an existing mechanism.
```

### Assumptions
 - The specification for the language is contained in a single file
 - The language's syntax is composed of two types of productions: expressions and commands.
 - The semantics program states are of the form *<command, memory, outputs>*