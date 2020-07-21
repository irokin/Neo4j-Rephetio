package uk.ac.ncl.utils;

import com.google.common.collect.Table;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import uk.ac.ncl.Settings;
import uk.ac.ncl.structs.MetaPath;
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

            System.out.println("# #Compounds: " + compounds.size());
            System.out.println("# #Disease: " + diseases.size());

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

            System.out.println("# #Positives: " + positives.size());
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

        System.out.println("# #Negatives: " + negatives.size());
        System.out.println("# #All: " + (negatives.size() + positives.size()));
    }

    public static void writeMatrix(Table<Triple, MetaPath, Double> table, File home) {
        File indexFile = new File(home, "data/metapath_index.txt");
        File matrixFile = new File(home, "data/matrix.txt");
        DecimalFormat f = new DecimalFormat("###.#####");

        List<String> header = new ArrayList<>();
        header.add("head");
        header.add("tail");
        header.add("label");

        List<MetaPath> list = new ArrayList<>(table.columnKeySet());
        try(PrintWriter writer = new PrintWriter(indexFile)) {
            for (int i = 0; i < list.size(); i++) {
                writer.println(i + "\t" + list.get(i).toOutString());
                header.add(String.valueOf(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        try(PrintWriter writer = new PrintWriter(matrixFile)) {
            writer.println(String.join("\t", header));
            for (Triple triple : table.rowKeySet()) {
                header.clear();
                header.add(triple.sub);
                header.add(triple.obj);
                header.add(triple.label ? "1" : "0");
                for (MetaPath metaPath : list) {
                    Double value = table.get(triple, metaPath);
                    header.add( value == null ? "0" : f.format(value));
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


}
