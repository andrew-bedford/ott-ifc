package ottifc.ott.semantics;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class State {
    String _state;

    //Where state is a string of the form <x := a, m, o> (i.e., <command, memory, outputs>
    public State(String state) {
        _state = state;
    }

    //TODO Generate the regex patterns from the syntax instead of using a hard-coded one. The hard-coded one is used only for the proof-of-concept.
    public boolean containsExpressions() {
        Pattern p = Pattern.compile("(x[0-9\\']?)|(n[0-9\\']?)|(a[0-9\\']?)|(b[0-9\\']?)|(c[0-9\\']?)|true|false");
        Matcher m = p.matcher(_state);
        return m.find();
    }

    //TODO See containsExpression's TODO
    public boolean containsCommands() {
        Pattern p = Pattern.compile("skip|x := a|x := n|cmd1 ; cmd2|skip ; cmd2|while b do cmd end|if b then cmd1 else cmd2 end|read x from c|write x to c|cmd[0-9\\']?");
        Matcher m = p.matcher(_state);
        Boolean result = m.find();
        return result;
    }

    public String toString() {
        return _state;
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

    public String getCommand() {
        String[] s = _state.split(",");
        return s[0].substring(1, s[0].length()); //Removes the "<" at the beginning
    }

    public String getAbstractCommand() {
        String command = getCommand();
        String abstractCommand = command.replaceAll("[\\d']","");
        return abstractCommand;
    }

    public String getMemory() {
        String[] s = _state.split(",");
        return s[1];
    }

    public String getOutput() {
        String[] s = _state.split(",");
        return s[2];
    }


    public Boolean isMemoryModified() {
        String memory = getMemory();
        if (memory.contains("|->")) {
            return true;
        }
        return false;
    }

    public Boolean isOutputModified() {
        String output = getOutput();
        if (output.contains("::")) {
            return true;
        }
        return false;
    }

    public void addUpdateToEnvironment(String variable, String value) {
        _state = _state.replace(", E", String.format(", E[%s |-> %s]", variable, value));
    }

    public Set<String> getModifiedVariables() {
        Set<String> modifiedVariables = new HashSet<>();
        if (isMemoryModified()) { //Contains at least one update |->
            String memory = getMemory();
            String[] memoryUpdates = memory.split("\\|->");
            for(String memoryUpdate : memoryUpdates) {
                if (memoryUpdate.contains("[")) {
                    String updatedVariable = memoryUpdate.trim().split("\\[")[1].trim();
                    modifiedVariables.add(updatedVariable); //Take the right-hand side after [, it should contain the variable's name
                }
            }
        }
        return modifiedVariables;
    }

    public Set<String> getModifiedVariablesWithoutChannels() {
        Set<String> modifiedVariables = new HashSet<>();
        if (isMemoryModified()) { //Contains at least one update |->
            String memory = getMemory();
            String[] memoryUpdates = memory.split("\\|->");
            for(String memoryUpdate : memoryUpdates) {
                if (memoryUpdate.contains("[")) {
                    String updatedVariable = memoryUpdate.trim().split("\\[")[1].trim();
                    if (!updatedVariable.equals("ch")) {
                        modifiedVariables.add(updatedVariable); //Take the right-hand side after [, it should contain the variable's name
                    }
                }
            }
        }
        return modifiedVariables;
    }

    public void insertVariable(String s) {
        _state = _state.replaceAll("<(.*?[^-])>", String.format("<$1, %s>", s));
    }

    @Override public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof State))return false;

        State otherState = (State)other;

        if (!this._state.equals(otherState._state)) { return false; }

        return true;
    }

    @Override public int hashCode() {
        return _state.hashCode();
    }
}
