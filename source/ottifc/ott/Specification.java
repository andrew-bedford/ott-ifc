package ottifc.ott;

import ottifc.ott.semantics.Rule;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Specification {
    String _specification;
    String _stepSymbol;
    List<Rule> _rules; //We use a list to preserve the original rule order
    Set<String> _commandNonTerminals;
    Set<String> _expressionNonTerminals;
    Set<String> _reservedNonTerminals = new HashSet<>(Arrays.asList("memory", "m", "output", "o", "formula", "terminals"));

    public Specification(String specification) {
        _specification = specification;
        removeAnnotationsFromSpecification();
        removeCommentsFromSpecification();
        _stepSymbol = extractStepSymbol();
        _rules = extractRules();
        _commandNonTerminals = extractCommandNonTerminals();
        _expressionNonTerminals = extractExpressionNonTerminals();
    }

    public String getAbstractCommandOfRule(Rule r) {
        Set<String> commandNonTerminals = getCommandNonTerminals();
        for (String cnt : commandNonTerminals) {
            Set<String> abstractCommands = getAbstractProductions(cnt);
            for(String ac : abstractCommands) {
                List<Rule> rulesForAC = getRules(ac);
                if (rulesForAC.contains(r)) {
                    return ac;
                }
            }
        }

        return "";
    }

    private Set<String> extractCommandNonTerminals() {
        Set<String> metaVars = getNonTerminals();
        Set<String> commands = new HashSet<>();
        for (String mv : metaVars) {
            if (isCommandNonTerminal(mv)) {
                commands.add(mv);
            }
        }

        return commands;
    }

    private Set<String> extractExpressionNonTerminals() {
        Set<String> metaVars = getNonTerminals();
        Set<String> expressions = new HashSet<>();
        for (String mv : metaVars) {
            if (isExpressionNonTerminal(mv) && !isReservedNonTerminal(mv)) {
                expressions.add(mv);
            }
        }

        return expressions;
    }

    private boolean isReservedNonTerminal(String nonTermianl) {
        return _reservedNonTerminals.contains(nonTermianl);
    }

    public Set<String> getCommandNonTerminals() { return _commandNonTerminals; }

    public Set<String> getExpressionNonTerminals() { return _expressionNonTerminals; }

    public List<Rule> getRules() {
        return _rules;
    }

    //TODO Define and use types for commands, expressions and non-terminals
    public List<Rule> getRules(String specificCommandExpressionOrNonTerminal) {
        List<Rule> rulesRelatedToSpecificProduction = new LinkedList<>();
        Set<String> possibleProductions;
        if (isNonTerminal(specificCommandExpressionOrNonTerminal)) {
            possibleProductions = getUnfoldedPossibleProductionsForNonTerminal(specificCommandExpressionOrNonTerminal);
        }
        else {
            possibleProductions = getUnfoldedPossibleProductions(specificCommandExpressionOrNonTerminal);
        }

        for (Rule r : getRules()) {
            String commandInInitialState = r.getInitialState().getAbstractCommand();
            if (possibleProductions.contains(commandInInitialState)) {
                rulesRelatedToSpecificProduction.add(r);
            }
        }

        return rulesRelatedToSpecificProduction;
    }

    public Set<Rule> getCommandRules() {
        Set<Rule> commandRules = new HashSet<>();
        Set<String> commandNonTerminals = getCommandNonTerminals();
        for(String cnt : commandNonTerminals) {
            commandRules.addAll(getRules(cnt));
        }

        return commandRules;
    }

    public boolean isCommandRule(Rule r) {
        Set<Rule> commandRules = getCommandRules();
        return commandRules.contains(r);
    }

    public Set<String> getMetaVars() {
        return getVars("metavar");
    }

    public Set<String> getIndexVars() {
        return getVars("indexvar");
    }


    /**
     *
     * @param vartype "metavar" or "indexvar"
     * @return The set of variables present in the file header
     *
     * Example:
     * If the header is:
     *     metavar integer, n ::= {{ coq nat }} {{ lex  numeral }}
     *     indexvar index, i, j ::= {{ coq nat }}
     * then getVars("metavars") would return {integer, n}
     */
    private Set<String> getVars(String vartype) {
        Set<String> setOfVars = new HashSet<>();

        String[] specLines = _specification.split(System.getProperty("line.separator"));
        for(String line : specLines) {
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith(vartype)) {
                String[] metavars = trimmedLine.substring(trimmedLine.indexOf(" "), trimmedLine.length()).split("::=")[0].split(",");
                for (String metavar : metavars) {
                    setOfVars.add(metavar.trim());
                }
            }

        }

        return setOfVars;
    }

    /**
     * Removes annotations (i.e., blocks that have the form {{ ... }}) from the specification so that they do not interfere with our analysis
     */
    private void removeAnnotationsFromSpecification() {
        Pattern p = Pattern.compile("\\{\\{(.|\\s)*?\\}\\}", Pattern.MULTILINE);
        Matcher m = p.matcher(_specification);

        while (m.find()) {
            _specification = _specification.replace(m.group(), "");
        }
    }

    private void removeCommentsFromSpecification() {
        String lines[] = _specification.split(System.getProperty("line.separator"));
        String specificationWithoutComments = "";
        for(String line : lines) {
            if (!line.startsWith("%")) {
                specificationWithoutComments += line + System.getProperty("line.separator");
            }
        }
        _specification = specificationWithoutComments;

    }

    private List<Rule> extractRules() {
        List<Rule> rules = new ArrayList<Rule>();
        String[] specParagraphs = _specification.split(System.getProperty("line.separator")+System.getProperty("line.separator")); // We split the specification using \n\n because between each rule, there must be an additionnal \n
        for(String s: specParagraphs) {
            if (s.contains("-----") && (s.contains("||") || s.contains("-->"))) { //Then it is (probably) a semantics rule
                rules.add(new Rule(this, s));
            }
        }
        return rules;
    }

    private String extractStepSymbol() {
        String[] specParagraphs = _specification.split(System.getProperty("line.separator")+System.getProperty("line.separator")); // We split the specification using \n\n because between each rule, there must be an additionnal \n
        for(String s: specParagraphs) {
            if (s.contains("-----")) { //Then it is (probably) a semantics rule
                //For the moment, we assume that the step symbols used are either --> (usually used in small-step semantics) or || (usually used in big-step semantics)
                if (s.contains("-->")) { return "-->"; }
                else if (s.contains("||")) { return "||"; }
            }
        }
        System.err.println("Error: Non-supported step-relation symbol. Please use --> (small-step) or || (big-step). ");
        System.exit(1);
        return ""; //TODO Throw exception instead of stopping the program
    }

    public String getStepSymbol() {
        return _stepSymbol;
    }

    public Set<String> getNonTerminals() {
        Set<String> setOfNonTerminals = new HashSet<>();
        String[] specLines = _specification.split(System.getProperty("line.separator"));

        //Ott files are usually decomposed in three sections: meta/variables, grammar and definitions.
        Boolean inMetaVariablesSection = true;
        Boolean inGrammarSection = false;
        Boolean inDefinitionsSection = false;

        for (String line : specLines) {
            if (inMetaVariablesSection && (line.startsWith("metavar") || line.startsWith("indexvar"))) {
                //Lines in the metavar sections look like this: indexvar index, i, j ::= {{ coq nat }}
                String[] metavariables = line.replace("metavar", "").replace("indexvar","").replace(" ", "").split("::=")[0].split(","); //Extracts the "i" and "j" from the example above
                for (String m : metavariables) { setOfNonTerminals.add(m); }
            }

            if (inGrammarSection && !(line.trim().startsWith("%") || line.trim().startsWith("|")) && line.contains("::")) {
                //Lines in the grammar section look like this: arith_expr, a :: ae_ ::= | x ::  :: variable
                String[] nonTerminals = line.replace(" ", "").split("::")[0].split(",");
                for (String n : nonTerminals) { setOfNonTerminals.add(n); }
            }

            if (inDefinitionsSection) { break; }

            if (line.startsWith("grammar")) { inMetaVariablesSection = false; inGrammarSection = true; inDefinitionsSection = false; }
            if (line.startsWith("defns")) { inMetaVariablesSection = false; inGrammarSection = false; inDefinitionsSection = true; }
        }


        return setOfNonTerminals;
    }

    //Example: For boolean expressions, bool_expr ::= true | false | a1 < a2, it returns the set {true, false, a < a}
    public Set<String> getAbstractProductions(String nonTerminal) {
        Set<String> setOfAbstractProductions = new HashSet<>();
        String[] specLines = _specification.split(System.getProperty("line.separator"));
        Boolean inGrammarSection = false;
        Boolean foundNonTerminal = false;

        for (String line : specLines) {
            if (inGrammarSection && !(line.trim().startsWith("%") || line.trim().startsWith("|")) && line.contains("::")) {
                if (!foundNonTerminal) {
                    //Lines in the grammar section look like this: arith_expr, a :: ae_ ::= | x ::  :: variable
                    String[] nonTerminals = line.replace(" ", "").split("::")[0].split(",");
                    for (String n : nonTerminals) {
                        if (n.equals(nonTerminal)) {
                            foundNonTerminal = true;
                            break;
                        }
                    }
                }
                else {
                    break;
                }
            }

            if (foundNonTerminal && line.trim().startsWith("|")) {
                String production = line.split("\\|")[1].split("::")[0].trim();
                String abstractProduction = production.replaceAll("[\\d']",""); //Assumes that none of the non-terminals have digits in the middle of their names
                setOfAbstractProductions.add(abstractProduction);
            }

            if (line.startsWith("grammar")) { inGrammarSection = true; }
            if (line.startsWith("defns")) { inGrammarSection = false; break; }
        }
        return setOfAbstractProductions;
    }

    public boolean isNonTerminal(String word) {
        Set<String> nonTerminals = getNonTerminals();
        return nonTerminals.contains(word);
    }

    public boolean isMetaOrIndexVariable(String word) {
        Set<String> metaVars = getMetaVars();
        Set<String> indexVars = getIndexVars();
        if (metaVars.contains(word) || indexVars.contains(word)) { return true; }

        return false;
    }

    //For example, isCommand("cmd") should return true
    public boolean isCommandNonTerminal(String nonTerminal) {
        if (!isNonTerminal(nonTerminal)) { return false; }
        if (isReservedNonTerminal(nonTerminal)) { return false; }

        //If it is not an expression, then it is (probably) a command
        return !isExpressionNonTerminal(nonTerminal);
    }

    public boolean isExpressionNonTerminal(String nonTerminal) {
        if (!isNonTerminal(nonTerminal)) { return false; }
        if (isReservedNonTerminal(nonTerminal)) { return false; }

        Set<String> possibleValues = getUnfoldedPossibleProductionsForNonTerminal(nonTerminal);
        for(Rule r : getRules()) {
            //We verify if the rule is for this non-terminal
            if (possibleValues.contains(r.getInitialState().getCommand())) {
                if (r.getFinalState().isMemoryModified() || r.getFinalState().isOutputModified()) {
                    //The rule modifies the memory, hence the non-terminal cannot be an expression
                    return false;
                }
            }
        }

        //None of the rules modified the memory
        return true;
    }

    public Set<String> getUnfoldedPossibleCommands() {
        Set<String> result = new HashSet<>();
        Set<String> commandNonTerminals = getCommandNonTerminals();
        for (String cnt : commandNonTerminals) {
            result.addAll(getUnfoldedPossibleProductionsForNonTerminal(cnt));
        }
        return result;
    }

    //TODO Find better variable and function names
    //For example, for the "if b then cmd else cmd end", will return { "if b then...", "if true then...", "if false then...", ... }
    public Set<String> getUnfoldedPossibleProductions(String production) {
        Set<String> possibleValues = new HashSet<>();
        Set<String> nonTerminalsToUnfold = getNonTerminalsPresentInAbstractProduction(production);
        possibleValues.add(production);

        for(String nonTerminalToUnfold : nonTerminalsToUnfold) {
            Set<String> possibleProductionsOfNonTerminalToUnfold = getUnfoldedPossibleProductionsForNonTerminal(nonTerminalToUnfold);
            for (String p : possibleProductionsOfNonTerminalToUnfold) {
                possibleValues.add(production.replace(nonTerminalToUnfold+ " ", p + " "));
                possibleValues.add(production.replace(" " + nonTerminalToUnfold, " " + p));
                possibleValues.add(production.replace(" " + nonTerminalToUnfold +" ", " " + p + " "));
            }
        }

        return possibleValues;
    }

    public Set<String> getUnfoldedPossibleProductionsForNonTerminal(String nonTerminal) {
        Set<String> possibleProductions = getAbstractProductions(nonTerminal);

        //Step 1 : Unfolds (once) the occurences of "nonTerminal" in the abstract productions of "nonTerminal" (e.g., a = {x, a + a} will produce a = {x, a + a, x + a, a + x, x + x}
        Set<String> unfoldedProductions = new HashSet<>(possibleProductions);
        Set<String> nonTerminalsToUnfold = new HashSet<>();
        for(String p1 : possibleProductions) {
            nonTerminalsToUnfold.addAll(getNonTerminalsPresentInAbstractProduction(p1));
            for (String p2 : possibleProductions) {
                unfoldedProductions.add(p2.replace(nonTerminal+ " ", p1 + " "));
                unfoldedProductions.add(p2.replace(" " + nonTerminal, " " + p1));
                unfoldedProductions.add(p2.replace(" " + nonTerminal +" ", " " + p1 + " "));
            }
        }

        //Step 2 : Unfolds (once) the occurences of other non-terminals in the productions
        Set<String> unfoldedProductionsIncludingAllNonTerminals = new HashSet<>(unfoldedProductions);
        for (String nonTerminalToUnfold : nonTerminalsToUnfold) {
            if (!nonTerminalToUnfold.equals(nonTerminal) && !isMetaOrIndexVariable(nonTerminalToUnfold)) {
                //FIXME Could become stuck in an infinite look if, in the productions, "b" can be "a" and "a" can be "b"
                Set<String> possibleProductionsOfNonTerminalToUnfold = getUnfoldedPossibleProductionsForNonTerminal(nonTerminalToUnfold);
                for(String p1 : unfoldedProductions) {
                    for (String p2 : possibleProductionsOfNonTerminalToUnfold) {
                        unfoldedProductionsIncludingAllNonTerminals.add(p1.replace(nonTerminalToUnfold+ " ", p2 + " "));
                        unfoldedProductionsIncludingAllNonTerminals.add(p1.replace(" " + nonTerminalToUnfold, " " + p2));
                        unfoldedProductionsIncludingAllNonTerminals.add(p1.replace(" " + nonTerminalToUnfold +" ", " " + p2 + " "));
                    }
                }
            }
        }

        return unfoldedProductionsIncludingAllNonTerminals;
    }

    //For example, the abstract production "a < a" would return the set {a}
    public Set<String> getNonTerminalsPresentInAbstractProduction(String s) {
        Set<String> results = new HashSet<>();
        String[] words = s.split(" ");
        for (String word : words) {
            if (isNonTerminal(word)) {
                results.add(word);
            }
        }

        return results;
    }

    public void print() {
        System.out.println(_specification);
    }
}
