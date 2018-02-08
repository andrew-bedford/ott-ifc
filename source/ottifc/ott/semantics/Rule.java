package ottifc.ott.semantics;

import helpers.StringHelper;
import ottifc.ott.Specification;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {
    Specification _spec;
    String _rule;
    State _initialState;
    State _finalState;
    String _separator;
    String _stepSymbol;
    List<String> _preconditions;
    String _lastEnvironmentSuffix;

    public Rule(Specification spec, String rule) {
        _spec = spec;
        _rule = rule;
        _preconditions = extractPreconditions();
        _separator = extractSeparator();
        _initialState = extractInitialState();
        _finalState = extractFinalState();
        _lastEnvironmentSuffix = "";
    }

    public List<String> getPreconditions() {
        return _preconditions;
    }

    public void addPrecondition(String precondition) {
        _preconditions.add(precondition);
    }

    public void removePrecondition(String precondition) {
        _preconditions.remove(precondition);
    }

    public void setPreconditions(List<String> preconditions) {
        _preconditions = preconditions;
    }

    public State getInitialState() {
        return _initialState;
    }
    public void setInitialState(State newInitialState) { _initialState = newInitialState; }

    public State getFinalState() {
        return _finalState;
    }
    public void setFinalState(State newFinalState) { _finalState = newFinalState; }

    public String toString() {
        return _rule;
    }

    public void print() {
        for(String precondition : _preconditions) {
            System.out.println(precondition);
        }
        System.out.println(_separator);
        System.out.println(String.format("%s %s %s", _initialState.toString(), _stepSymbol, _finalState.toString()));

    }

    public void insertIntoAllCommandStates(String s) {
        getInitialState().insertVariable(s);
        getFinalState().insertVariable(s);

        List<String> newPreconditions = new LinkedList<>();

        for(String precondition : _preconditions) {
            if (precondition.contains(_spec.getStepSymbol())) {
                String iniSt = StringHelper.getStringWithoutNumbersOrApostrophes(precondition.split(_spec.getStepSymbol().replace("|","\\|"))[0]);
                State initialState = new State(iniSt);
                String initialStateCommand = initialState.getAbstractCommand();
                Set<String> specificationPossibleCommands = _spec.getUnfoldedPossibleCommands();
                if (_spec.isCommandNonTerminal(initialStateCommand) || specificationPossibleCommands.contains(initialStateCommand)) {
                    precondition = precondition.replaceAll("<(.*?[^-])>", String.format("<$1, %s>", s));
                }
            }

            newPreconditions.add(precondition);
        }
        _preconditions = newPreconditions;
    }

    private List<String> extractPreconditions() {
        List<String> preconditions = new LinkedList<>();
        String s = _rule.split("----")[0];
        String[] lines = s.split(System.getProperty("line.separator"));
        for(String line : lines) {
            if (!line.trim().isEmpty()) {
                preconditions.add(line.trim());
            }
        }
        return preconditions;
    }

    private String extractSeparator() {
        String lines[] = _rule.split(System.getProperty("line.separator"));
        String separator = "";
        for(String line : lines) {
            if (line.startsWith("----")) {
                separator = line;
            }
        }
        return separator;
    }

    public Set<String> getExpressionVariablesUsedInPreconditions() {
        Set<String> expressionVariables = new HashSet<>();
        Set<String> expressionNonTerminals = _spec.getExpressionNonTerminals();
        for(String precondition : _preconditions) {
            for (String ent : expressionNonTerminals) {

                Pattern p = Pattern.compile("[^\\w]("+ent+"[0-9\\']?)");
                Matcher m;

                //TODO Move this test elsewhere, don't think that it should be this function's responsibility to filter the expression variables
                // If the precondition of the form : <a, m, o, pc, E> --> <a', m, o, pc, E>, return the expression variables only present in the initial state so that the algo adds E |- a : l_a, but not E |- a' : l_a'
                if (precondition.contains(_spec.getStepSymbol())) {
                    m = p.matcher(precondition.split(_spec.getStepSymbol().replace("|", "\\|"))[0]);
                }
                else {
                    m = p.matcher(precondition);
                }
                while (m.find()) {
                    expressionVariables.add(m.group(1));
                }
            }
        }
        return expressionVariables;
    }

    /*
    Example: The final state returned corresponds to "c"
    a              a
    -------   or   -------
    b --> c        b || c
    */
    private State extractFinalState() {
        String s[] = _rule.split(System.getProperty("line.separator"));
        String finalState = "";
        String lastLine = s[s.length-1];
        if (lastLine.contains("-->")) {
            finalState = s[s.length - 1].split("-->")[1].trim();
        }
        else if (lastLine.contains("||")) {
            finalState = s[s.length - 1].split("\\|\\|")[1].trim();
        }
        return new State(finalState);
    }


    /*
    Example: The initial state returned corresponds to "b"
    a              a
    -------   or   -------
    b --> c        b || c
    */
    private State extractInitialState() {
        String s[] = _rule.split(System.getProperty("line.separator"));
        String finalState = "";
        String lastLine = s[s.length-1].trim();
        if (lastLine.contains("-->")) {
            _stepSymbol = "-->";
            finalState = s[s.length - 1].split("-->")[0].trim();
        }
        else if (lastLine.contains("||")) {
            _stepSymbol = "||";
            finalState = s[s.length - 1].split("\\|\\|")[0].trim();
        }
        return new State(finalState);
    }

    @Override public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Rule))return false;

        Rule otherRule = (Rule)other;

        if (!this._rule.equals(otherRule._rule)) { return false; }

        return true;
    }

    @Override public int hashCode() {
        return _rule.hashCode();
    }

    public void insertProgramCounters() {
        insertIntoAllCommandStates("pc");
    }

    public void insertEnvironments() {
        List<String> newPreconditions = new LinkedList<>();
        for(String precondition : _preconditions) {
            if (precondition.contains(_spec.getStepSymbol())) {
                State initialState = new State(precondition.split(_spec.getStepSymbol().replace("|","\\|"))[0]);
                State finalState = new State(precondition.split(_spec.getStepSymbol().replace("|","\\|"))[1]);
                String initialStateAbstractCommand = initialState.getAbstractCommand();
                Set<String> specificationPossibleCommands = _spec.getUnfoldedPossibleCommands();
                if (_spec.isCommandNonTerminal(initialStateAbstractCommand) || specificationPossibleCommands.contains(initialStateAbstractCommand)) {
                    //We ensure that the environment names follows the naming convention of the user's memory and output variables.
                    //For example, if we have <cmd2, m1, o1> || <stop, m2, o2>, then the inserted environment variables should be E1 in the initial state and E2 in the final state.
                    String initialEnvironmentSuffix = StringHelper.getStringWithoutLetters(initialState.getMemory()).trim();
                    precondition = precondition.replaceAll("<(.*?[^-])> ((-->)|(\\|\\|))", String.format("<$1, %s%s> $2", "E", initialEnvironmentSuffix));

                    String finalEnvironmentSuffix = StringHelper.getStringWithoutLetters(finalState.getMemory()).trim();
                    precondition = precondition.replaceAll("((-->)|(\\|\\|)) <(.*?[^-])>", String.format("$1 <$4, %s%s>", "E", finalEnvironmentSuffix));

                    _lastEnvironmentSuffix = finalEnvironmentSuffix;
                }
            }

            newPreconditions.add(precondition);
        }
        _preconditions = newPreconditions;

        String initialEnvironmentSuffix = StringHelper.getStringWithoutLetters(getInitialState().getMemory()).trim();
        getInitialState().insertVariable("E"+initialEnvironmentSuffix);

        getFinalState().insertVariable("E"+_lastEnvironmentSuffix);
    }
}
