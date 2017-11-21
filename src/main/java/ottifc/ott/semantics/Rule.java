package ottifc.ott.semantics;

public class Rule {
    String _rule;
    State _initialState;
    State _finalState;

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
        return null;
    }

    private State extractInitialState() {
        return null;
    }

}
