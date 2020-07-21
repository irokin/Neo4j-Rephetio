package uk.ac.ncl.utils;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import uk.ac.ncl.Settings;

public class Quality {

    public static double PDP(Path path, double dump) {
        double pdp = 1;
        try(Transaction tx = Settings.getCurrentGraph().beginTx()) {
            for (Relationship rel : path.relationships()) {
                int startD = rel.getStartNode().getDegree(rel.getType(), Direction.BOTH);
                int endD = rel.getEndNode().getDegree(rel.getType(), Direction.BOTH);
                pdp *= ((double) 1 / Math.pow(startD, dump)) * ((double) 1 / Math.pow(endD, dump));
            }
            tx.success();
        }
        return pdp;
    }


}
