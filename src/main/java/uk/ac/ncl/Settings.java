package uk.ac.ncl;

import org.neo4j.graphdb.GraphDatabaseService;
import uk.ac.ncl.structs.MetaPath;
import uk.ac.ncl.structs.Triple;
import uk.ac.ncl.utils.Logging;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

public class Settings {

    public static void report() {
        Logging.println(MessageFormat.format("# Settings:\n" +
                "# Identifier: {0} | Depth: {1}\n" +
                "# Target: {2} | Threads: {3}\n" +
                        "# Inverse: {4} | Random Walker: {5}\n" +
                        "# EntityType {6}\n",
                identifier,
                depth,
                target,
                threads,
                allowInverse,
                rw,
                entityType));
    }

    public static String identifier = "name";
    public static int depth = 4;
    public static String target = "PUBLISHES";
    public static int threads = 6;
    public static int rw = 150;
    public static File home;
    public static boolean allowInverse = false;
    public static boolean entityType = true;

    private static GraphDatabaseService currentGraph;
    public static void setCurrentGraph(GraphDatabaseService graph) {
        currentGraph = graph;
//        Logging.println("# Set Current Graph #");
    }
    public static GraphDatabaseService getCurrentGraph() {
        if(currentGraph == null) {
            System.err.println("Error: Graph database is requested before init.");
            System.exit(-1);
        }
        return currentGraph;
    }

    public static int processed = 0;
    public static void resetProcessed() {
        processed = 0;
    }
    public static synchronized void updateProcessed() {
        processed++;
        if(processed % 500 == 0) {
            Logging.println("####### Processed " + processed + " triples ########");
        }
    }

    public static Set<Triple> emptyTriples = new HashSet<>();
    public static synchronized void updateEmptyTriples(Triple triple) {
        emptyTriples.add(triple);
    }
    public static void resetEmptyTriples() {
        emptyTriples.clear();
    }
}
