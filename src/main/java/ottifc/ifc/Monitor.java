package ottifc.ifc;

import ottifc.ott.Specification;
import ottifc.ott.semantics.Rule;

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
        for(Rule r : rules) {
            if (r.getInitialState().containsCommands()) {

                Set<String> expressionVariables = r.getExpressionVariablesUsedInPreconditionsWithoutConstants();
                insertLabelDefinitions(r, expressionVariables);

                Set<String> modifiedVariables = r.getFinalState().getModifiedVariablesWithoutChannels();
                insertEnvironmentUpdates(r, expressionVariables, modifiedVariables);

                if (r.getFinalState().isOutputModified()) {
                    Set<String> modifiedChannels = r.getFinalState().getModifiedVariables();
                    modifiedChannels.removeAll(modifiedVariables);
                    for(String modifiedChannel : modifiedChannels) {
                        String guard = getSupremumOfSet(expressionVariables) + " |_| pc <= " + modifiedChannel;
                        r.addPrecondition(guard);
                    }

                }

                r.print();
                System.out.println("");
            }
        }



        //_spec.print();
    }

    private void insertEnvironmentUpdates(Rule r, Set<String> expressionVariables, Set<String> modifiedVariables) {
        for(String modifiedVariable : modifiedVariables) {
            r.getFinalState().addUpdateToEnvironment(modifiedVariable, getSupremumOfSet(expressionVariables));
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
                r.insertIntoAllStates("pc");
            }
        }
        for(Rule r : _spec.getRules()) {
            r.insertIntoAllStates("E"); //The typing environment is always inserted when generating the monitor
        }
    }
}
