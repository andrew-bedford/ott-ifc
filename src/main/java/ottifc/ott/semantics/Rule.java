package ottifc.ott.semantics;

import ottifc.ott.Specification;

public class Rule {
    String _rule;
    State _initialState;
    State _finalState;
    String[] _preconditions;

    public Rule(String rule) {
        _rule = rule;

        _initialState = extractInitialState();
        _finalState = extractFinalState();
    }

    //FIXME Temporary. Instead of simply searching for ::, compare the last variables of the states to see if they are the same
    public boolean modifiesOutputTrace(State initialState, State finalState) {
        String s[] = _rule.split("\n");
        return s[s.length-1].contains("::");
    }

    public void print() {
        System.out.println(_rule);
    }

    private State extractFinalState() {
        String s[] = _rule.split("\n");
        String finalState = s[s.length-1].split("-->")[1];
        return new State(finalState);
    }

    private State extractInitialState() {
        return null;
    }

}
