package ottifc;

import helpers.FileHelper;
import helpers.ParameterHelper;
import ottifc.ifc.Monitor;
import ottifc.ifc.Option;
import ottifc.ott.Specification;
import ottifc.ott.semantics.Rule;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ParameterHelper.parse(args);

        if (ParameterHelper.isEmpty() || ParameterHelper.contains("-help") || ParameterHelper.contains("h")) {
            System.out.println("Usage:");
            System.out.println("  -i [.ott file]                       Specification to use as input.");
            System.out.println("  -m [generation | verification]       Ott-IFC's mode. Use 'generation' to generate a mechanism and 'verification' to verify an existing mechanism.");
        }
        //Note: We use the same parameters as Ott for the input file: "-i"
        else if (ParameterHelper.contains("i")) {
            String filePath = ParameterHelper.get("i", 0);
            

            FileHelper.verifyFileExists(filePath);
            String fileContents = FileHelper.convertFileToString(new File(filePath));

            System.out.println("---------------------------------------------");
            System.out.println("|                    ott                    |");
            System.out.println("---------------------------------------------");
            System.out.println(fileContents);

            System.out.println("---------------------------------------------");
            System.out.println("|                  ott-ifc                  |");
            System.out.println("---------------------------------------------");
            Specification spec = new Specification(fileContents);

            //spec.getUnfoldedPossibleProductionsForNonTerminal("i");
            //Set<String> asd = spec.getNonTerminalsPresentInAbstractProduction("if b then cmd else cmd end");
            System.out.println("Expression? a = " + spec.isExpression("a"));
            System.out.println("Expression? b = " + spec.isExpression("b"));
            System.out.println("Expression? cmd = " + spec.isExpression("cmd"));
            System.out.println("Command? cmd = " + spec.isCommand("cmd"));

            //Monitor m = new Monitor(spec, EnumSet.of(Option.EXPLICIT_FLOWS, Option.IMPLICIT_FLOWS));
            //m.generate();

        }
        else if (ParameterHelper.contains("m")) {
            String selectedMode = ParameterHelper.get("m", 0);

            switch (selectedMode) {
                case "generation": //This mode is used to generate an information-flow control mechanism
                    break;
                case "verification": //This mode is used to verify an existing information-flow control mechanism
                    break;
            }
        }

    }




}
