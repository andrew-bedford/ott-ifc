package ottifc.ott.semantics;

import ottifc.ott.Specification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {
    String _rule;
    State _initialState;
    State _finalState;
    String _separator;
    String _stepSymbol;
    Set<String> _preconditions;

    public Rule(String rule) {
        _rule = rule;
        _preconditions = extractPreconditions();
        _separator = extractSeparator();
        _initialState = extractInitialState();
        _finalState = extractFinalState();
    }

    public Set<String> getPreconditions() {
        return _preconditions;
    }

    public void addPrecondition(String precondition) {
        _preconditions.add(precondition);
    }

    public void removePrecondition(String precondition) {
        _preconditions.remove(precondition);
    }

    public void setPreconditions(Set<String> preconditions) {
        _preconditions = preconditions;
    }

    public State getInitialState() {
        return _initialState;
    }

    public State getFinalState() {
        return _finalState;
    }

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

    public void insertIntoAllStates(String s) {
        getInitialState().insertVariable(s);
        getFinalState().insertVariable(s);

        Set<String> newPreconditions = new HashSet<>();

        for(String precondition : _preconditions) {
            precondition = precondition.replaceAll("<", String.format("<%s, ", s));
            newPreconditions.add(precondition);

            //precondition = precondition.replaceAll("--> <", String.format("--> <%s, ", s));
            //precondition = precondition.replaceAll("\\|\\| <", String.format("|| <%s, ", s));
        }
        _preconditions = newPreconditions;

    }

    private Set<String> extractPreconditions() {
        Set<String> preconditions = new HashSet<String>();
        String s = _rule.split("----")[0];
        String[] lines = s.split("\r\n");
        for(String line : lines) {
            if (!line.trim().isEmpty()) {
                preconditions.add(line.trim());
            }
        }
        return preconditions;
    }

    private String extractSeparator() {
        String lines[] = _rule.split("\r\n");
        String separator = "";
        for(String line : lines) {
            if (line.startsWith("----")) {
                separator = line;
            }
        }
        return separator;
    }

    //FIXME Temporary. Instead of simply searching for ::, compare the last variables of the states to see if they are the same
    public boolean modifiesOutputTrace(State initialState, State finalState) {
        String s[] = _rule.split("\r\n");
        return s[s.length-1].contains("::");
    }

    public Set<String> getExpressionVariablesUsedInPreconditions() {
        Set<String> expressionVariables = new HashSet<>();
        for(String precondition: _preconditions) {
            Pattern p = Pattern.compile("(x[0-9\\']?)|(n[0-9\\']?)|(a[0-9\\']?)|(b[0-9\\']?)|(ch[0-9\\']?)|true|false");
            Matcher m = p.matcher(precondition);
            while (m.find()) {
                expressionVariables.add(m.group());
            }
        }
        return expressionVariables;
    }

    public Set<String> getExpressionVariablesUsedInPreconditionsWithoutConstants() {
        Set<String> expressionVariables = getExpressionVariablesUsedInPreconditions();
        Set<String> filteredSet = new HashSet<>();
        for(String expressionVariable : expressionVariables) {
            if (!expressionVariable.startsWith("true") && !expressionVariable.startsWith("false") && !expressionVariable.startsWith("n")) {
                filteredSet.add(expressionVariable);
            }
        }
        return filteredSet;
    }

    private State extractFinalState() {
        String s[] = _rule.split("\r\n");
        String finalState = "";
        String lastLine = s[s.length-1];
        if (lastLine.contains("-->")) {
            finalState = s[s.length - 1].split("-->")[1];
        }
        else if (lastLine.contains("||")) {
            finalState = s[s.length - 1].split("\\|\\|")[1];
        }
        return new State(finalState);
    }

    private State extractInitialState() {
        String s[] = _rule.split("\r\n");
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

}
