package uk.ac.ncl.structs;

import org.neo4j.graphdb.*;
import uk.ac.ncl.Settings;

import java.util.ArrayList;
import java.util.List;

public class MetaPath {

    public List<Direction> directions = new ArrayList<>();
    public List<String> nodes = new ArrayList<>();
    public List<String> relationships = new ArrayList<>();

    public int getLength() {
        return relationships.size();
    }

    public MetaPath(Path path) {
        GraphDatabaseService graph = Settings.getCurrentGraph();
        try (Transaction tx = graph.beginTx()) {
            List<Node> neo4jNodes = new ArrayList<>();
            path.nodes().forEach(neo4jNodes::add);
            for (int i = 0; i < neo4jNodes.size(); i++) {
                if(Settings.entityType)
                    nodes.add(neo4jNodes.get(i).getLabels().iterator().next().name());
                else
                    nodes.add("V" + i);
            }
            List<Relationship> neo4jRels = new ArrayList<>();
            path.relationships().forEach(neo4jRels::add);
            neo4jRels.forEach(rel -> relationships.add(rel.getType().name()));

            for (int i = 0; i < neo4jRels.size(); i++) {
                Relationship currentRel = neo4jRels.get(i);
                if(currentRel.getStartNode().equals(neo4jNodes.get(i)))
                    directions.add(Direction.OUTGOING);
                else
                    directions.add(Direction.INCOMING);
            }
            tx.success();
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MetaPath) {
            MetaPath other =(MetaPath) obj;
            return this.toString().equals(other.toString());
        }
        return false;
    }

    @Override
    public String toString() {
        List<String> words = new ArrayList<>();
        if(Settings.allowInverse) {
            for (int i = 0; i < directions.size(); i++) {
                if(directions.get(i).equals(Direction.OUTGOING))
                    words.add(relationships.get(i) + "(" + nodes.get(i) + ", " + nodes.get(i + 1) + ")");
                else
                    words.add(relationships.get(i) + "(" + nodes.get(i + 1) + ", " + nodes.get(i ) + ")");
            }
        } else {
            for (int i = 0; i < relationships.size(); i++) {
//                words.add(relationships.get(i) + "(" + nodes.get(i) + ", " + nodes.get(i + 1) + ")");
                words.add(nodes.get(i));
                words.add(relationships.get(i));
            }
            words.add(nodes.get(nodes.size() - 1));
        }
        return String.join(",", words);
    }

    public String toOutString() {
        List<String> words = new ArrayList<>();
        if(Settings.allowInverse) {
            for (int i = 0; i < directions.size(); i++) {
                if (directions.get(i).equals(Direction.OUTGOING))
                    words.add(relationships.get(i));
                else
                    words.add("inv_" + relationships.get(i));
            }
        } else {
            words.addAll(relationships);
        }
        return String.join("\t", words);
    }
}
