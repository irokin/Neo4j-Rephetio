package uk.ac.ncl.utils;

import com.google.common.collect.Table;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
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

    public static void addUMLStoDisorder() {
        GraphDatabaseService graph = Settings.getCurrentGraph();
        int nodeCount = 0;
        int haveNameCount = 0;
        try(Transaction tx = graph.beginTx()) {
            for (Node node : graph.getAllNodes()) {
                nodeCount++;
                if(node.getLabels().iterator().next().name().equals("Disorder")) {
                    String[] names = (String[]) node.getProperty("domainIds");
                    for (String name : names) {
                        if(name.contains("umls")) {
                            haveNameCount++;
                            node.setProperty("primaryDomainId", name);
                        }
                    }
                }
            }
            tx.success();
        }
        System.out.println(nodeCount);
        System.out.println(haveNameCount);
    }


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

            System.out.println("# #Drugs: " + compounds.size());
            System.out.println("# #Diseases: " + diseases.size());

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

            System.out.println("# #Positives: " + positives.size());
            tx.success();
        }

        try(PrintWriter writer = new PrintWriter(posFile)) {
            positives.forEach(triple -> writer.println(triple.tabSepString()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("# #All: " + (positives.size()));
    }

    public static void writeMatrix(Table<Triple, MetaPath, Double> table, File out) {
        File indexFile = new File(out, "metapath_index.txt");
        File matrixFile = new File(out, "matrix.txt");
        DecimalFormat f = new DecimalFormat("###.#####");

        List<String> header = new ArrayList<>();
        header.add("head");
        header.add("tail");
        header.add("label");

        System.out.println("# Metapaths: " + table.columnKeySet().size());
        System.out.println("# Valid Triples: " + table.rowKeySet().size());

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
