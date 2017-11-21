package ottifc.ott.semantics;

public class State {
    String _state;

    //Where state is a string of the form <x := a, m, o> (i.e., <command, memory, outputs>
    public State(String state) {
        _state = state;
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
