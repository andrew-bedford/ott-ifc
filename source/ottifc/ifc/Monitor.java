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
        Set<Rule> commandsAffectingControlFlow = getRulesWhichMayAffectControlFlow();
        for(Rule r : rules) {
            if (_spec.isCommandRule(r)) {

                Set<String> expressionVariables = r.getExpressionVariablesUsedInPreconditions();
                insertLabelDefinitions(r, expressionVariables);

                Set<String> modifiedVariables = r.getFinalState().getModifiedVariablesWithoutChannels();
                insertEnvironmentUpdates(r, expressionVariables, modifiedVariables);

                addGuards(r, expressionVariables, modifiedVariables);

                //If the current rule involves one of the commands that may affect the control-flow
                if (commandsAffectingControlFlow.contains(r)) {
                    List<String> newPreconditions = new LinkedList<>();
                    for(String precondition : r.getPreconditions()) {
                        if (!expressionVariables.isEmpty() && containsCommandNonTerminal(precondition)) {
                            newPreconditions.add(precondition.replaceAll("pc", "pc |_| " + getSupremumOfSet(expressionVariables)));
                        }
                        else {
                            newPreconditions.add(precondition);
                        }
                    }

                    r.setPreconditions(newPreconditions);

                    if (!expressionVariables.isEmpty() && !r.getFinalState().getAbstractCommand().equals("stop")) {
                        String programCounterWithUpdate = r.getFinalState().getProgramCounter().replace("pc", "pc |_| " + getSupremumOfSet(expressionVariables));
                        r.getFinalState().setProgramCounter(programCounterWithUpdate);
                    }

                }

                r.print();
                System.out.println("");
            }
        }
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
                String guard = getSupremumOfSet(expressionVariables) + " |_| pc <= " + "l_"+modifiedChannel;
                r.addPrecondition(guard);
            }

        }
    }

    private Set<Rule> getRulesWhichMayAffectControlFlow() {
        Set<String> commandNonTerminals = _spec.getCommandNonTerminals();
        Set<Rule> rulesWhichMayAffectControlFlow = new HashSet<>();
        for(String cnt : commandNonTerminals) {
            Set<String> abstractCommands = _spec.getAbstractProductions(cnt);
            for(String ac : abstractCommands) {
                List<Rule> rulesForCommand = _spec.getRules(ac);
                if (rulesForCommand.size() >= 2) { //If there are multiple rules for the same command, then we assume that it may affect the control-flow of the application
                    rulesWhichMayAffectControlFlow.addAll(rulesForCommand);
                }
            }
        }

        return rulesWhichMayAffectControlFlow;
    }

    private void insertEnvironmentUpdates(Rule r, Set<String> expressionVariables, Set<String> modifiedVariables) {
        for(String modifiedVariable : modifiedVariables) {
            if (!expressionVariables.isEmpty()) {
                r.getFinalState().addUpdateToEnvironment(modifiedVariable, "pc |_| " + getSupremumOfSet(expressionVariables));
            }
            else {
                r.getFinalState().addUpdateToEnvironment(modifiedVariable, "pc");
            }
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
