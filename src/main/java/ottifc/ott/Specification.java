package ottifc.ott;

import java.util.HashSet;
import java.util.Set;

public class Specification {
    String _specification;

    public Specification(String specification) {
        _specification = specification;
    }

    //TODO Use enum for the vartype?
    public Set<String> getVars(String vartype) {
        Set<String> setOfVars = new HashSet<>();

        String[] specLines = _specification.split("\n");
        for(String line : specLines) {
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith(vartype)) {
                String[] metavars = trimmedLine.substring(trimmedLine.indexOf(" "), trimmedLine.length()).split("::=")[0].split(",");
                for (String metavar : metavars) {
                    setOfVars.add(metavar.trim());
                    System.err.println(String.format("%s : %s", vartype, metavar.trim()));
                }
            }
        }

        return setOfVars;
    }

    public Set<String> getNonTerminals() {
        Set<String> setOfNonTerminals = new HashSet<>();
        String[] specLines = _specification.split("\n");

    }
}
