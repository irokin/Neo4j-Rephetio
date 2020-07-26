import com.google.common.collect.Table;
import org.junit.Test;
import uk.ac.ncl.*;
import uk.ac.ncl.model.Rephetio;
import uk.ac.ncl.structs.MetaPath;
import uk.ac.ncl.structs.Triple;
import uk.ac.ncl.utils.IO;

import java.io.File;

public class test {
    @Test
    public void run() {
        File config = new File("data/Repotrial/config.json");
        Rephetio system = new Rephetio(config);
        system.buildFeatureMatrix();
    }

    @Test
    public void RephetioTest() {
        Settings.report();

        File file = new File("data/UWCSE/data/train.txt");
        File graphFile = new File("data/UWCSE/databases/graph.db");

        Rephetio system = new Rephetio(file);
        Table<Triple, MetaPath, Double> table = system.buildFeatureMatrix();

        System.out.println("Triples: " + system.triples.length() + " | Rows: " + table.rowKeySet().size());
    }

    @Test
    public void RephetioHetTest() {
        Settings.threads = 8;
        Settings.target = "TREATS_CtD";
        Settings.identifier = "identifier";
        Settings.report();

        File home = new File("data/het/data");
        File file = new File("data/het/data/positives.txt");
        File graphFile = new File("data/het/databases/graph.db");

        Rephetio system = new Rephetio(file);
        Table<Triple, MetaPath, Double> table = system.buildFeatureMatrix();

        System.out.println("Meta-paths: " + table.columnKeySet().size());
        System.out.println("Triples: " + system.triples.length() + " | Rows: " + table.rowKeySet().size());

        IO.writeMatrix(table, home);
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
