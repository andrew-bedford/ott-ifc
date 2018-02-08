package helpers;

import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerComponentNameProvider;
import org.jgrapht.ext.StringComponentNameProvider;
import org.jgrapht.graph.DefaultEdge;

import java.io.FileWriter;
import java.io.IOException;

public class GraphHelper {
    public static void exportToDotFile(DirectedGraph<String, DefaultEdge> graph, String path) {
        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<String, DefaultEdge>(new IntegerComponentNameProvider(), new StringComponentNameProvider<String>(), null);
        try {
            exporter.exportGraph(graph,  new FileWriter(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
