import org.junit.Test;
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
        File config = new File("data/Repotrial/config.json");
        Rephetio system = new Rephetio(config);
        system.buildCandidateMatrix(system.buildFeatureMatrix());
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
}
