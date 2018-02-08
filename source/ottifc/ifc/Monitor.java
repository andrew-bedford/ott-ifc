package ottifc.ifc;

import helpers.DebugHelper;
import helpers.StringHelper;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
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
                insertEnvironmentUpdates(r, expressionVariables);
                addGuards(r, expressionVariables);

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
            for (String abstractCommand : _spec.getAbstractProductions(cnt)) {
                DirectedGraph<Rule, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
                DirectedGraph<String, DefaultEdge> graph2 = new DefaultDirectedGraph<>(DefaultEdge.class);
                List<Rule> rules = _spec.getRules(abstractCommand);
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
                commandsToGraphs.put(abstractCommand, graph);
                DebugHelper.println("Command graph of " + abstractCommand + ": ");
                DebugHelper.println(graph.toString()+"\n\n");
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

    private boolean ruleHasSuccessorThatModifiesMemory(DirectedGraph graph, Rule rule) {
        GraphIterator<Rule, DefaultEdge> iterator = new DepthFirstIterator<Rule, DefaultEdge>(graph, rule);
        while (iterator.hasNext()) {
            Rule r = iterator.next();
            if (r.getFinalState().isMemoryModified()) {
                return true;
            }
        }
        return false;
    }

    private boolean ruleHasSuccessorThatModifiesOutput(DirectedGraph graph, Rule rule) {
        GraphIterator<Rule, DefaultEdge> iterator = new DepthFirstIterator<Rule, DefaultEdge>(graph, rule);
        while (iterator.hasNext()) {
            Rule r = iterator.next();
            if (r.getFinalState().isOutputModified()) {
                return true;
            }
        }
        return false;
    }

    private void addGuards(Rule r, Set<String> expressionVariables) {
        Set<String> modifiedVariables = r.getFinalState().getModifiedVariablesWithoutChannels();

        if (r.getFinalState().isOutputModified()) { //Or if one of its succesors in the command graph produces an output
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
                Set<Rule> terminalRules = getSetOfTerminalRules(_commandsToGraphs.get(ac));
                //System.out.println("Terminal Rules for command '"+ac+"':");
                //System.out.println(terminalRules.toString());
                //System.out.println("\n");
                if (terminalRules.size() >= 2) { //If there are multiple rules for the same command, then we assume that it may affect the control-flow of the application
                    List<Rule> rulesForCommand = _spec.getRules(ac);
                    rulesWhichMayAffectControlFlow.addAll(rulesForCommand); //FIXME Not all rules for that command should be included, only maybe the ones a branch occurs
                }
            }
        }

        return rulesWhichMayAffectControlFlow;
    }

    private void insertEnvironmentUpdates(Rule r, Set<String> expressionVariables) {
        Set<String> modifiedVariables = r.getFinalState().getModifiedVariablesWithoutChannels();
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

    //Note: "Terminal rules" are vertices in the graph that have no outgoing edges.
    public Set<Rule> getSetOfTerminalRules(DirectedGraph<Rule, DefaultEdge> graph) {
        Set<Rule> setOfTerminalVertices = new HashSet<>();
        GraphIterator<Rule, DefaultEdge> iterator = new DepthFirstIterator<Rule, DefaultEdge>(graph);
        while (iterator.hasNext()) {
            Rule r = iterator.next();
            Set<DefaultEdge> e = graph.outgoingEdgesOf(r);
            if (e.isEmpty()) {
                setOfTerminalVertices.add(r);
            }
        }

        return setOfTerminalVertices;
    }
}
