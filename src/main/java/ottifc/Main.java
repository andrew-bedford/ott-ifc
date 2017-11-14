package ottifc;

import helpers.FileHelper;
import helpers.ParameterHelper;
import ottifc.ott.Specification;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        ParameterHelper.parse(args);

        if (ParameterHelper.isEmpty() || ParameterHelper.contains("-help")) {
            //TODO
            System.out.println("Display HELP");
        }
        //Note: We use the same parameter as Ott for the input file "-i"
        else if (ParameterHelper.contains("i")) {
            String filePath = ParameterHelper.get("i", 0);

            verifyFileExists(filePath);
            String fileContents = FileHelper.convertFileToString(new File(filePath));

            System.out.println("---------------------------------------------");
            System.out.println("|             Original ott                  |");
            System.out.println("---------------------------------------------");
            System.out.println(fileContents);


            System.out.println("---------------------------------------------");
            System.out.println("|                 ott-ifc                   |");
            System.out.println("---------------------------------------------");
            Specification spec = new Specification(fileContents);
            spec.getVars("metavar");
            spec.getVars("indexvar");
            System.out.println(fileContents.replaceAll("<", "<G, pc, "));
        }

    }

    private static void verifyFileExists(String filePath) {
        if (!FileHelper.fileExists(filePath)) {
            System.err.println("Error: File '"+filePath+"' not found.");
            System.exit(1);
        }
    }
}
