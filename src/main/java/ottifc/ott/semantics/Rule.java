package ottifc.ott.semantics;

import ottifc.ott.Specification;

import java.util.ArrayList;
import java.util.List;

public class Rule {
    String _rule;
    State _initialState;
    State _finalState;
    String _separator;
    List<String> _preconditions;

    public Rule(String rule) {
        _rule = rule;
        _preconditions = extractPreconditions();
        _separator = extractSeparator();
        _initialState = extractInitialState();
        _finalState = extractFinalState();
    }

    private List<String> extractPreconditions() {
        List<String> preconditions = new ArrayList<String>();

    }

    private String extractSeparator() {
        String lines[] = _rule.split("\r\n");
        String separator = "";
        for(String line : lines) {
            if (line.startsWith("---")) {
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

    public void print() {
        System.out.println(_rule);
    }

    private State extractFinalState() {
        String s[] = _rule.split("\r\n");
        String finalState = "";
        String lastLine = s[s.length-1];
        if (lastLine.contains("-->")) {
            finalState = s[s.length - 1].split("-->")[1];
        }
        else if (lastLine.contains("||")) {
            finalState = s[s.length - 1].split("||")[1];
        }
        return new State(finalState);
    }

    private State extractInitialState() {
        String s[] = _rule.split("\r\n");
        String finalState = "";
        String lastLine = s[s.length-1].trim();
        if (lastLine.contains("-->")) {
            finalState = s[s.length - 1].split("-->")[0].trim();
        }
        else if (lastLine.contains("||")) {
            finalState = s[s.length - 1].split("||")[0].trim();
        }
        return new State(finalState);
    }

}
