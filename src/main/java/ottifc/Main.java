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

            Monitor m = new Monitor(spec, EnumSet.of(Option.EXPLICIT_FLOWS, Option.IMPLICIT_FLOWS));
            m.generate();
        }

    }

    private static void verifyFileExists(String filePath) {
        if (!FileHelper.fileExists(filePath)) {
            System.err.println("Error: File '"+filePath+"' not found.");
            System.exit(1);
        }
    }

    //TODO Generate the regex patterns from the syntax instead of using a hard-coded one. The hard-coded one is used only for the proof-of-concept.
    public boolean containsExpression(String s) {
        Pattern p = Pattern.compile("(x[0-9\\']?)|(n[0-9\\']?)|(a[0-9\\']?)|(b[0-9\\']?)|true|false");
        Matcher m = p.matcher(s);
        return m.matches();

    }

    //TODO See containsExpression's TODO
    public boolean containsCommand(String s) {
        Pattern p = Pattern.compile("skip|x := a|x := n|c1 ; c2|while b do c end|if b then|(c[0-9\\']?)");
        Matcher m = p.matcher(s);
        return m.matches();
    }
}
