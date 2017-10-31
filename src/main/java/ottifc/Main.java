package ottifc;

import helpers.FileHelper;
import helpers.ParameterHelper;

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
            System.out.println("|             Original Ott                  |");
            System.out.println("---------------------------------------------");
            System.out.println(fileContents);
        }

    }

    private static void verifyFileExists(String filePath) {
        if (!FileHelper.fileExists(filePath)) {
            System.err.println("Error: File '"+filePath+"' not found.");
            System.exit(1);
        }
    }
}
