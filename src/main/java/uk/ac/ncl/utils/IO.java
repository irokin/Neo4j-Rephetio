package uk.ac.ncl.utils;

import com.google.common.collect.Table;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import uk.ac.ncl.Settings;
import uk.ac.ncl.structs.MetaPath;
import uk.ac.ncl.structs.Prediction;
import uk.ac.ncl.structs.Triple;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class IO {

    public static void generateInstancesHet(File out) {
        GraphDatabaseService graph = Settings.getCurrentGraph();
        File posFile = new File(out, "positives.txt");
        File negFile = new File(out, "negatives.txt");

        Set<String> compounds = new HashSet<>();
        Set<String> diseases = new HashSet<>();
        Set<Triple> positives = new HashSet<>();
        Set<Triple> negatives = new HashSet<>();
        String target = "TREATS_CtD";

        try(Transaction tx = graph.beginTx()) {
            for (Node node : graph.getAllNodes()) {
                String label = node.getLabels().iterator().next().name();
                if(label.equals("Compound") && node.getDegree() > 0)
                    compounds.add((String) node.getProperty("identifier"));

                if(label.equals("Disease") && node.getDegree() > 0)
                    diseases.add((String) node.getProperty("identifier"));
            }

            Logging.println("# #Compounds: " + compounds.size());
            Logging.println("# #Disease: " + diseases.size());

            for (Relationship rel : graph.getAllRelationships()) {
                String type = rel.getType().name();
                if(type.equals("TREATS_CtD")) {
                    positives.add(
                            new Triple((String) rel.getStartNode().getProperty("identifier")
                                    , type
                                    , (String) rel.getEndNode().getProperty("identifier"), true)
                    );
                }
            }

            Logging.println("# #Positives: " + positives.size());
            tx.success();
        }

        for (String compound : compounds) {
            for (String disease : diseases) {
                Triple triple = new Triple(compound, target, disease);
                if(!positives.contains(triple)) {
                    negatives.add(triple);
                }
            }
        }

        try(PrintWriter writer = new PrintWriter(posFile)) {
            positives.forEach(triple -> writer.println(triple.tabSepString()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        try(PrintWriter writer = new PrintWriter(negFile)) {
            negatives.forEach(triple -> writer.println(triple.tabSepString()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Logging.println("# #Negatives: " + negatives.size());
        Logging.println("# #All: " + (negatives.size() + positives.size()));
    }

    public static void generateInstancesRepo(File out) {
        GraphDatabaseService graph = Settings.getCurrentGraph();
        File posFile = new File(out, "positives.txt");
        File negFile = new File(out, "negatives.txt");
        String target = "DrugHasIndication";

        List<String> negs = new ArrayList<>();
        try(LineIterator l = FileUtils.lineIterator(negFile)) {
            while(l.hasNext()) {
                negs.add(l.next());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try(PrintWriter w = new PrintWriter(negFile)) {
            for (String neg : negs) {
                String[] words = neg.split("\t");
                w.println(words[0] + "\t" + target + "\t" + words[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }


        Set<String> compounds = new HashSet<>();
        Set<String> diseases = new HashSet<>();
        Set<Triple> positives = new HashSet<>();

        try(Transaction tx = graph.beginTx()) {
            for (Node node : graph.getAllNodes()) {
                String label = node.getLabels().iterator().next().name();
                if(label.equals("BiotechDrug") && node.getDegree() > 0)
                    compounds.add((String) node.getProperty("primaryDomainId"));

                if(label.equals("SmallMoleculeDrug") && node.getDegree() > 0)
                    compounds.add((String) node.getProperty("primaryDomainId"));

                if(label.equals("Disorder") && node.getDegree() > 0)
                    diseases.add((String) node.getProperty("primaryDomainId"));
            }

            Logging.println("# #Drugs: " + compounds.size());
            Logging.println("# #Diseases: " + diseases.size());

            for (Relationship rel : graph.getAllRelationships()) {
                String type = rel.getType().name();
                if(type.equals("DrugHasIndication")) {
                    positives.add(
                            new Triple((String) rel.getStartNode().getProperty("primaryDomainId")
                                    , type
                                    , ((String) rel.getEndNode().getProperty("primaryDomainId")), true)
                    );
                }
            }

            Logging.println("# #Positives: " + positives.size());
            tx.success();
        }

        try(PrintWriter writer = new PrintWriter(posFile)) {
            positives.forEach(triple -> writer.println(triple.tabSepString()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Logging.println("# #All: " + (positives.size()));
    }

    public static void writeCandidateMatrix(Table<Triple, MetaPath, Double> table, File out) {
        File matrixFile = new File(out, "candidate_matrix.txt");
        DecimalFormat f = new DecimalFormat("###.#####");
        Set<Triple> emptyTriples = Settings.emptyTriples;

        List<String> header = new ArrayList<>();
        header.add("head");
        header.add("tail");

        Logging.println("# Metapaths: " + table.columnKeySet().size());
        Logging.println("# Candidates: " +
                table.rowKeySet().size() +
                " | Empty Candidates: " +
                emptyTriples.size() +
                " | All: " + (table.rowKeySet().size() + emptyTriples.size()));

        List<MetaPath> list = new ArrayList<>(table.columnKeySet());
        for (int i = 0; i < list.size(); i++) {
            header.add(String.valueOf(i));
        }

        try(PrintWriter writer = new PrintWriter(matrixFile)) {
            writer.println(String.join("\t", header));
            for (Triple triple : table.rowKeySet()) {
                header.clear();
                header.add(triple.sub);
                header.add(triple.obj);
                for (MetaPath metaPath : list) {
                    Double value = table.get(triple, metaPath);
                    header.add( value == null ? "0" : f.format(value));
                }
                writer.println(String.join("\t", header));
            }
            for (Triple triple : emptyTriples) {
                header.clear();
                header.add(triple.sub);
                header.add(triple.obj);
                for (MetaPath metaPath : list) {
                    header.add("0");
                }
                writer.println(String.join("\t", header));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void writeMatrix(Table<Triple, MetaPath, Double> table, File out) {
        File rawRuleFile = new File(out, "ranta_rawRuleId.txt");
        File ruleFile = new File(out, "ranta_ruleId.txt");
        File matrixFile = new File(out, "ranta_matrix.txt");
        DecimalFormat f = new DecimalFormat("###.#####");
        Set<Triple> emptyTriples = Settings.emptyTriples;

        List<String> header = new ArrayList<>();
        header.add("as");
        header.add("head");
        header.add("tail");
        header.add("label");

        Logging.println("# Metapaths: " + table.columnKeySet().size());
        Logging.println("# Valid Triples: " +
                table.rowKeySet().size() +
                " | Empty Triples: " +
                emptyTriples.size() +
                " | All: " + (table.rowKeySet().size() + emptyTriples.size()));

        List<MetaPath> list = new ArrayList<>(table.columnKeySet());
        for (int i = 0; i < list.size(); i++) {
            list.get(i).index = i;
        }

        try(PrintWriter writer = new PrintWriter(ruleFile)) {
            try(PrintWriter rawWriter = new PrintWriter(rawRuleFile)) {
                for (int i = 0; i < list.size(); i++) {
                    MetaPath current = list.get(i);
                    writer.println(current.toRuleString());
                    rawWriter.println(current.toRawRuleString());
                    header.add(String.valueOf(current.index));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        try(PrintWriter writer = new PrintWriter(matrixFile)) {
            writer.println(String.join("\t", header));
            for (Triple triple : table.rowKeySet()) {
                header.clear();
                header.add(triple.mark);
                header.add(triple.sub);
                header.add(triple.obj);
                header.add(triple.label ? "1" : "0");
                for (MetaPath metaPath : list) {
                    Double value = table.get(triple, metaPath);
                    header.add( value == null ? "0" : f.format(value));
                }
                writer.println(String.join("\t", header));
            }
            for (Triple triple : emptyTriples) {
                header.clear();
                header.add(triple.mark);
                header.add(triple.sub);
                header.add(triple.obj);
                header.add(triple.label ? "1" : "0");
                for (MetaPath metaPath : list) {
                    header.add("0");
                }
                writer.println(String.join("\t", header));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static JSONObject buildJSONObject(File file) {
        JSONObject args = null;

        try (InputStream in = new FileInputStream(file)) {
            JSONTokener tokener = new JSONTokener(in);
            args = new JSONObject(tokener);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return args;
    }

    public static List<Prediction> readTopPredictions(File file) {
        List<Prediction> predictions = new ArrayList<>();
        try(LineIterator l = FileUtils.lineIterator(file)) {
            while(l.hasNext()) {
                String line = l.next();
                if(line.startsWith("drugbank")) {
                    String[] words = line.split("\t");
                    Prediction current = new Prediction(words[0], words[1], Double.parseDouble(words[2]));
                    predictions.add(current);
//                    if(l.hasNext() && l.next().startsWith("positive")) {
                    if(l.hasNext()) {
                        while(l.hasNext()) {
                            String nextLine = l.next();
//                            if(nextLine.startsWith("negative") || nextLine.isEmpty())
//                                break;
                            if(nextLine.isEmpty())
                                break;
                            String[] nextWords = nextLine.split("\t");
                            current.positiveEvidences.put(nextWords[0], Double.parseDouble(nextWords[1]));
                        }
//                        while(l.hasNext()) {
//                            String nextLine = l.next();
//                            if(nextLine.isEmpty())
//                                break;
//                            String[] nextWords = nextLine.split("\t");
//                            current.negativeEvidences.put(nextWords[0], Double.parseDouble(nextWords[1]));
//                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return predictions;
    }

    public static GraphDatabaseService createEmptyGraph(File graphHome) {
        try {
            if (graphHome.exists())
                FileUtils.deleteDirectory(graphHome);
            graphHome.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        File databaseFile = new File(graphHome, "databases/graph.db");
        GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(databaseFile);
        Runtime.getRuntime().addShutdownHook(new Thread(graph::shutdown));
        return graph;
    }

    public static void copyNodes(Node left, Node right) {
        for (Map.Entry<String, Object> entry : left.getAllProperties().entrySet()) {
            right.setProperty(entry.getKey(), entry.getValue());
        }
        for (Label label : left.getLabels()) {
            right.addLabel(label);
        }
    }

}
