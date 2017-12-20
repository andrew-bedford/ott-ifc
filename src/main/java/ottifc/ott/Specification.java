package ottifc.ott;

import ottifc.ott.semantics.Rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Specification {
    String _specification;
    String _stepSymbol;
    List<Rule> _rules; //We use a list to preserve the original rule order

    public Specification(String specification) {
        _specification = specification;
        removeAnnotationsFromSpecification();
        removeCommentsFromSpecification();
        _stepSymbol = extractStepSymbol();
        _rules = extractRules();
    }

    public List<Rule> getRules() {
        return _rules;
    }

    //TODO Use enum for the vartype?
    public Set<String> getVars(String vartype) {
        Set<String> setOfVars = new HashSet<>();

        String[] specLines = _specification.split(System.getProperty("line.separator"));
        for(String line : specLines) {
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith("grammar")) {
                System.out.println("GRAMMAR BEGINS : " + trimmedLine);
            }
            else if (trimmedLine.startsWith("defns")) {
                System.out.println("DEFINITIONS BEGINS : " + trimmedLine);
            }
            if (trimmedLine.startsWith(vartype)) {
                String[] metavars = trimmedLine.substring(trimmedLine.indexOf(" "), trimmedLine.length()).split("::=")[0].split(",");
                for (String metavar : metavars) {
                    setOfVars.add(metavar.trim());
                    System.err.println(String.format("%s : %s", vartype, metavar.trim()));
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
                rules.add(new Rule(s));
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
    public Set<String> getProductions(String nonTerminal) {
        Set<String> setOfNonTerminals = new HashSet<>();
        String[] specLines = _specification.split(System.getProperty("line.separator"));
        return null;
    }

    public void print() {
        System.out.println(_specification);
    }
}
