package uk.ac.ncl.model;

import org.json.JSONObject;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import uk.ac.ncl.structs.Prediction;
import uk.ac.ncl.utils.IO;

import java.io.File;
import java.util.*;

public class PredictionGraph {

    GraphDatabaseService graph;
    GraphDatabaseService predGraph;
    List<Prediction> predictionList;
    Map<String, Long> node2Id = new HashMap<>();
    final String target;

    public PredictionGraph(File configFile) {
        JSONObject args = IO.buildJSONObject(configFile);
        File home = configFile.getParentFile();
        File graphFile = new File(home, "databases/graph.db");
        File predGraphFile = new File(home, args.getString("predGraph"));
        File predictionFile = new File(home, args.getString("predFile"));
        target = args.getString("target");

        graph = new GraphDatabaseFactory().newEmbeddedDatabase(graphFile);
        Runtime.getRuntime().addShutdownHook(new Thread(graph::shutdown));

        predGraph = IO.createEmptyGraph(predGraphFile);
        predictionList = IO.readTopPredictions(predictionFile);

        String identifier = args.getString("identifier");
        try(Transaction tx = graph.beginTx()) {
            graph.getAllNodes().forEach(node -> node2Id.put((String) node.getProperty(identifier), node.getId()));
            tx.success();
        }

        for (Prediction p : predictionList) {
            assert node2Id.get(p.head) != null;
            assert node2Id.get(p.tail) != null;
            p.headId = node2Id.get(p.head);
            p.tailId = node2Id.get(p.tail);
        }
    }

    public void build() {
        System.out.println("# Start Building Prediction Graph:");

        Map<String, Long> createdNodes = new HashMap<>();
        try(Transaction tx = graph.beginTx()) {
            int count = 0;
            while(count < predictionList.size()) {
                try (Transaction predTx = predGraph.beginTx()) {
                    do {
                        Prediction prediction = predictionList.get(count);
                        Node sNode, eNode;
                        if(createdNodes.containsKey(prediction.head))
                            sNode = predGraph.getNodeById(createdNodes.get(prediction.head));
                        else {
                            sNode = predGraph.createNode();
                            IO.copyNodes(graph.getNodeById(prediction.headId), sNode);
                            createdNodes.put(prediction.head, sNode.getId());
                        }

                        if(createdNodes.containsKey(prediction.tail))
                            eNode = predGraph.getNodeById(createdNodes.get(prediction.tail));
                        else {
                            eNode = predGraph.createNode();
                            IO.copyNodes(graph.getNodeById(prediction.tailId), eNode);
                            createdNodes.put(prediction.tail, eNode.getId());
                        }

                        Relationship targetRel = sNode.createRelationshipTo(eNode, RelationshipType.withName(target));
                        targetRel.setProperty("confidence", prediction.confidence);

                        for (Map.Entry<String, Double> posEntry : prediction.positiveEvidences.entrySet()) {
                            Relationship posRel = sNode.createRelationshipTo(eNode,
                                    RelationshipType.withName("PositiveEvidence"));
                            posRel.setProperty("contribution", posEntry.getValue());
                            posRel.setProperty("motif", posEntry.getKey());
                        }

//                        for (Map.Entry<String, Double> posEntry : prediction.negativeEvidences.entrySet()) {
//                            Relationship posRel = sNode.createRelationshipTo(eNode,
//                                    RelationshipType.withName("NegativeEvidence"));
//                            posRel.setProperty("contribution", posEntry.getValue());
//                            posRel.setProperty("motif", posEntry.getKey());
//                        }

                        count++;
                    } while (count % 500 != 0);
                    System.out.println("# Populated " + count + " predictions");
                    predTx.success();
                }
            }
            tx.success();
        }
    }
}
