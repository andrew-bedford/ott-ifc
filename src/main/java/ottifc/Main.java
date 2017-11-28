package ottifc;

import helpers.FileHelper;
import helpers.ParameterHelper;
import ottifc.ifc.Monitor;
import ottifc.ifc.Option;
import ottifc.ott.Specification;

import java.io.File;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ParameterHelper.parse(args);

        if (ParameterHelper.isEmpty() || ParameterHelper.contains("-help") || ParameterHelper.contains("h")) {
            //TODO
            System.out.println("Display HELP");
        }
        //Note: We use the same parameters as Ott for the input file: "-i" or "--input"
        else if (ParameterHelper.contains("-input") || ParameterHelper.contains("i")) {
            String filePath = ParameterHelper.get("input", 0);
            if (filePath.isEmpty()) { filePath = ParameterHelper.get("i", 0); }

            verifyFileExists(filePath);
            String fileContents = FileHelper.convertFileToString(new File(filePath));

            System.err.println("---------------------------------------------");
            System.err.println("|             Original ott                  |");
            System.err.println("---------------------------------------------");
            Thread.sleep(100);
            System.out.println(fileContents);


            System.err.println("---------------------------------------------");
            System.err.println("|                 ott-ifc                   |");
            System.err.println("---------------------------------------------");
            Thread.sleep(100);
            Specification spec = new Specification(fileContents);
            //spec.getVars("metavar");
            //spec.getVars("indexvar");

            Monitor m = new Monitor(spec, EnumSet.of(Option.EXPLICIT_FLOWS, Option.IMPLICIT_FLOWS));
            m.generate();
        }
        else if (ParameterHelper.contains("-mode") || ParameterHelper.contains("m")) {
            String selectedMode = ParameterHelper.get("-mode", 0);
            if (selectedMode.isEmpty()) { selectedMode = ParameterHelper.get("m", 0); }

            switch (selectedMode) {
                case "generation": //This mode is used to generate an information-flow control mechanism
                    break;
                case "verification": //This mode is used to verify an existing information-flow control mechanism
                    break;
            }
        }

    }

    private static void verifyFileExists(String filePath) {
        if (!FileHelper.fileExists(filePath)) {
            System.err.println("Error: File '"+filePath+"' not found.");
            System.exit(1);
        }
    }


}
