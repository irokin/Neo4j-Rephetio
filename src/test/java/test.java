import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import uk.ac.ncl.Settings;
import uk.ac.ncl.model.PredictionGraph;
import uk.ac.ncl.model.Rephetio;
import uk.ac.ncl.structs.Prediction;
import uk.ac.ncl.utils.IO;
import uk.ac.ncl.utils.Logging;

import java.io.File;
import java.util.List;

public class test {
    @Test
    public void run() {
        File config = new File("data/Hetionet/config.json");
        Rephetio system = new Rephetio(config);
        system.buildFeatureMatrix();
    }

    @Test
    public void buildPredGraph() {
        File config = new File("data/Repotrial/pred_config.json");
        PredictionGraph system = new PredictionGraph(config);
        system.build();
    }

    @Test
    public void readPredictions() {
        File predictionFile = new File("data/Repotrial/r200/top_predictions.txt");
        List<Prediction> predictionList = IO.readTopPredictions(predictionFile);
        System.out.println(predictionFile.length());
    }

    @Test
    public void createEmptyGraph() {
        File graphFile = new File("data/Repotrial/r200/predGraph");
        IO.createEmptyGraph(graphFile);
    }

    @Test
    public void RephetioHetTest() {
        File config = new File("data/het/config.json");
        Rephetio system = new Rephetio(config);
        system.buildFeatureMatrix();
        Logging.report();
    }

    @Test
    public void generateInsForHet() {
        File graphFile = new File("data/het/databases/graph.db");
        Rephetio.loadGraph(graphFile);

        IO.generateInstancesHet(new File("data/het/data"));
    }

    @Test
    public void generateInsForRepo() {
        File graphFile = new File("data/Repotrial/databases/graph.db");
        Rephetio.loadGraph(graphFile);
//        IO.addUMLStoDisorder();
        IO.generateInstancesRepo(new File("data/Repotrial/data"));
    }

    @Test
    public void reportGraphInfo() {
        File graphFile = new File("data/Hetionet/databases/graph.db");
        Rephetio.loadGraph(graphFile);
        GraphDatabaseService graph = Settings.getCurrentGraph();
        try(Transaction tx =graph.beginTx()) {
            long relCount = graph.getAllRelationships().stream().count();
            System.out.println("Relationships: " + relCount);

            long nodes =  graph.getAllNodes().stream().count();
            System.out.println("Nodes: " + nodes);

            long relTypeCount = graph.getAllRelationshipTypes().stream().count();
            System.out.println("Relationship Types: " + relTypeCount);

            long nodeTypes = graph.getAllNodes().stream().count();
            System.out.println("Node Types: " + nodeTypes);

            tx.success();
        }
    }
}
