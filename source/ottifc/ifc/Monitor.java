package ottifc.ifc;

import ottifc.ott.Specification;
import ottifc.ott.semantics.Rule;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Monitor {
    Specification _spec;
    Set<Option> _options;

    public Monitor(Specification spec, Set<Option> options) {
        _spec = spec;
        _options = options;
    }

    private String getSupremumOfSet(Set<String> set) {
        String supremum = "";
        if (set.size() >= 1) {
            for (String element : set) {
                supremum += "l_"+element + " |_| ";
            }
            supremum = supremum.substring(0, supremum.length()-5); //Removes the last " |_| "
        }
        return supremum;
    }

    //Generates and outputs the monitor
    public void generate() {
        insertEnvironmentAndCounterInStates();

        List<Rule> rules = _spec.getRules();
        Set<String> commandsAffectingControlFlow = getCommandsWhichMayAffectControlFlow();
        for(Rule r : rules) {
            if (_spec.isCommandRule(r)) {

                Set<String> expressionVariables = r.getExpressionVariablesUsedInPreconditions();
                insertLabelDefinitions(r, expressionVariables);

                Set<String> modifiedVariables = r.getFinalState().getModifiedVariablesWithoutChannels();
                insertEnvironmentUpdates(r, expressionVariables, modifiedVariables);

                addGuards(r, expressionVariables, modifiedVariables);

                //If the current rule involves one of the commands that may affect the control-flow
                if (commandsAffectingControlFlow.contains(r.getInitialState().getCommand())) {
                    List<String> newPreconditions = new LinkedList<>();
                    for(String precondition : r.getPreconditions()) {
                        if (containsCommandNonTerminal(precondition)) {
                            newPreconditions.add(precondition.replaceAll("pc", "pc |_| " + getSupremumOfSet(expressionVariables)));
                        }
                        else {
                            newPreconditions.add(precondition);
                        }
                    }

                    r.setPreconditions(newPreconditions);
                }

                r.print();
                System.out.println("");
            }
        }



        //_spec.print();
    }

    private boolean containsCommandNonTerminal(String s) {
        for (String nt : _spec.getCommandNonTerminals()) {
            if (s.contains(nt)) { return true; }
        }
        return false;
    }

    //TODO Implement
    public void verify() {

    }

    private void addGuards(Rule r, Set<String> expressionVariables, Set<String> modifiedVariables) {
        if (r.getFinalState().isOutputModified()) {
            Set<String> modifiedChannels = r.getFinalState().getModifiedVariables();
            modifiedChannels.removeAll(modifiedVariables);
            for(String modifiedChannel : modifiedChannels) {
                String guard = getSupremumOfSet(expressionVariables) + " |_| pc <= " + modifiedChannel;
                r.addPrecondition(guard);
            }

        }
    }

    private Set<String> getCommandsWhichMayAffectControlFlow() {
        Set<String> commandSet = new HashSet<>();
        for(Rule r1 : _spec.getRules()) {
            if (_spec.isCommandRule(r1)) {
                //Look for another rule which has the same initial state, but a different final state
                //FIXME Is not quite sufficient, they could both modify the memory by executing different commands but have the "same" final state (i.e., both use the same variable names)
                for(Rule r2 : _spec.getRules()) {
                    if (!r1.equals(r2) && r1.getInitialState().equals(r2.getInitialState()) && !r1.getFinalState().equals(r2.getFinalState())) {
                        commandSet.add(r1.getInitialState().getCommand());
                    }
                }
            }
        }
        return commandSet;
    }

    private void insertEnvironmentUpdates(Rule r, Set<String> expressionVariables, Set<String> modifiedVariables) {
        for(String modifiedVariable : modifiedVariables) {
            r.getFinalState().addUpdateToEnvironment(modifiedVariable, "pc |_| " + getSupremumOfSet(expressionVariables));
        }
    }

    private void insertLabelDefinitions(Rule r, Set<String> expressionVariables) {
        for(String expressionVariable : expressionVariables) {
            r.addPrecondition(String.format("E |- %s : l_%s", expressionVariable, expressionVariable));
        }
    }


    private void insertEnvironmentAndCounterInStates() {
        if (_options.contains(Option.IMPLICIT_FLOWS)) { //The pc variable is necessary only to prevent implicit flows
            for(Rule r : _spec.getRules()) {
                r.insertProgramCounters();
            }
        }
        for(Rule r : _spec.getRules()) {
            r.insertEnvironments();

        }
    }
}
