package uk.ac.ncl;

import org.neo4j.graphdb.GraphDatabaseService;

import java.io.File;
import java.text.MessageFormat;

public class Settings {

    public static void report() {
        System.out.println(MessageFormat.format("# Settings:\n" +
                "# Identifier: {0} | Depth: {1}\n" +
                "# Target: {2} | Threads: {3}",
                identifier,
                depth,
                target,
                threads));
    }

    public static String identifier = "name";
    public static int depth = 4;
    public static String target = "PUBLISHES";
    public static int threads = 6;
    public static int rw = 150;
    public static File home;

    private static GraphDatabaseService currentGraph;
    public static void setCurrentGraph(GraphDatabaseService graph) {
        currentGraph = graph;
        System.out.println("# Set Current Graph #");
    }
    public static GraphDatabaseService getCurrentGraph() {
        if(currentGraph == null) {
            System.err.println("Error: Graph database is requested before init.");
            System.exit(-1);
        }
        return currentGraph;
    }

    public static int processed = 0;
    public static synchronized void updateProcessed() {
        processed++;
        if(processed % 500 == 0) {
            System.out.println("####### Processed " + processed + " triples ########");
        }
    }
}
