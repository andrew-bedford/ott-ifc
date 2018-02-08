package ottifc;

import helpers.DebugHelper;
import helpers.FileHelper;
import helpers.ParameterHelper;
import helpers.StringHelper;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerComponentNameProvider;
import org.jgrapht.ext.StringComponentNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import ottifc.ifc.Monitor;
import ottifc.ifc.Option;
import ottifc.ott.Specification;
import ottifc.ott.semantics.Rule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.jgrapht.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ParameterHelper.parse(args);

        if (ParameterHelper.isEmpty() || ParameterHelper.contains("-help") || ParameterHelper.contains("h")) {
            System.out.println("Usage:");
            System.out.println("  -i [.ott file]                       Specification to use as input.");
            System.out.println("  -m [generation | verification]       Ott-IFC's mode. Use 'generation' to generate a mechanism and 'verification' to verify an existing mechanism.");
            System.out.println("  -d                                   Enable debug mode");
        }
        if (ParameterHelper.contains("d")) {
            DebugHelper.enableDebugMode();
        }
        //Note: We use the same parameters as Ott for the input file: "-i"
        if (ParameterHelper.contains("i")) {
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

            Monitor m = new Monitor(spec, EnumSet.of(Option.EXPLICIT_FLOWS, Option.IMPLICIT_FLOWS));
            m.generate();
        }
        if (ParameterHelper.contains("m")) {
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
