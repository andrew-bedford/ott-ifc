package ottifc.ifc;

import helpers.StringHelper;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import ottifc.ott.Specification;
import ottifc.ott.semantics.Rule;

import java.util.*;

import org.jgrapht.*;

public class Monitor {
    Specification _spec;
    Set<Option> _options;
    Map<String, DirectedGraph<Rule, DefaultEdge>> _commandsToGraphs;

    public Monitor(Specification spec, Set<Option> options) {
        _spec = spec;
        _options = options;
        _commandsToGraphs = buildCommandGraphs();
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
        Set<Rule> rulesAffectingControlFlow = getRulesWhichMayAffectControlFlow();
        for(Rule r : rules) {
            if (_spec.isCommandRule(r)) {

                Set<String> expressionVariables = r.getExpressionVariablesUsedInPreconditions();
                insertLabelDefinitions(r, expressionVariables);

                Set<String> modifiedVariables = r.getFinalState().getModifiedVariablesWithoutChannels();
                insertEnvironmentUpdates(r, expressionVariables, modifiedVariables);

                addGuards(r, expressionVariables, modifiedVariables);

                //If the current rule involves one of the commands that may affect the control-flow
                if (rulesAffectingControlFlow.contains(r)) {
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

                    //If there are no expression variables, then the pc would not be updated. And if the final state is a "stop" command, then it would serve no purpose to "execute" the stop with an updated pc
                    if (!expressionVariables.isEmpty() && !isStopCommand(r.getFinalState().getAbstractCommand())) {
                        String programCounterWithUpdate = r.getFinalState().getProgramCounter().replace("pc", "pc |_| " + getSupremumOfSet(expressionVariables));
                        r.getFinalState().setProgramCounter(programCounterWithUpdate);
                    }

                }

                r.print();
                System.out.println("");
            }
        }
    }

    private Map<String, DirectedGraph<Rule, DefaultEdge>> buildCommandGraphs() {
        Map<String, DirectedGraph<Rule, DefaultEdge>> commandsToGraphs = new HashMap<>();
        for (String cnt : _spec.getCommandNonTerminals()) {
            for (String command : _spec.getAbstractProductions(cnt)) {
                DirectedGraph<Rule, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
                DirectedGraph<String, DefaultEdge> graph2 = new DefaultDirectedGraph<>(DefaultEdge.class);
                List<Rule> rules = _spec.getRules(command);
                for(Rule r1 : rules) {
                    graph.addVertex(r1);
                    List<Rule> rulesThatMayBeEvaluatedAfterRuleR1 = _spec.getRules(r1.getInitialState().getAbstractCommand());
                    for(Rule r2 : rulesThatMayBeEvaluatedAfterRuleR1) {
                        graph.addVertex(r2);
                        if (!r1.equals(r2) && !r1.getInitialState().getAbstractCommand().equals(r2.getInitialState().getAbstractCommand())) {
                            graph.addEdge(r1, r2);
                        }
                    }
                }
                commandsToGraphs.put(command, graph);
                System.out.println("For command " + command + ": ");
                System.out.println(graph.toString()+"\n\n");
            }
        }
        return commandsToGraphs;
    }

    public boolean isStopCommand(String s) {
        Set<String> possibleCommands = _spec.getUnfoldedPossibleCommands();
        List<Rule> rulesForCommand = _spec.getRules(s);
        Set<String> nonTerminalsPresent = _spec.getNonTerminalsPresentInAbstractProduction(StringHelper.getStringWithoutNumbersOrApostrophes(s));

        //A command is considered to be a "stop" command if (1) it is a command, (2) there are no semantics rule for this command and (3) it contains no non-terminals. If it fails to meet these three requirements, then we assume that it is not a "stop" command.
        if (!possibleCommands.contains(s) || !rulesForCommand.isEmpty() || !nonTerminalsPresent.isEmpty()) { return false; }

        return true;
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
