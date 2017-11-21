package ottifc.ott.semantics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class State {
    String _state;

    //Where state is a string of the form <x := a, m, o> (i.e., <command, memory, outputs>
    public State(String state) {
        _state = state;
    }

    //TODO Generate the regex patterns from the syntax instead of using a hard-coded one. The hard-coded one is used only for the proof-of-concept.
    public boolean containsExpressions(String s) {
        Pattern p = Pattern.compile("(x[0-9\\']?)|(n[0-9\\']?)|(a[0-9\\']?)|(b[0-9\\']?)|true|false");
        Matcher m = p.matcher(s);
        return m.matches();

    }

    //TODO See containsExpression's TODO
    public boolean containsCommands(String s) {
        Pattern p = Pattern.compile("skip|x := a|x := n|c1 ; c2|while b do c end|if b then|(c[0-9\\']?)");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public void print() {
        System.out.println(_state);
    }

    //TODO Do this in a cleaner fashion
    //We verify if a state is valid by verifying the number of variables it has
    private boolean isValid() {
        String[] s = _state.split(",");
        return (s.length == 3 || s.length == 5); //There are either 3 variables or 5 variables (when the environment and pc variables are added)
    }
}
