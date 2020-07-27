package uk.ac.ncl.model;

import com.google.common.collect.*;
import org.json.JSONObject;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.*;
import uk.ac.ncl.utils.IO;
import uk.ac.ncl.utils.Quality;
import uk.ac.ncl.Settings;
import uk.ac.ncl.structs.MetaPath;
import uk.ac.ncl.structs.Triple;
import uk.ac.ncl.structs.TripleSet;

import java.io.File;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Rephetio {

    public final TripleSet triples;
    public GraphDatabaseService graph;
    File outFile;

    int startDepth, endDepth;

    Map<String, Long> nodeIndex = new HashMap<>();

    public static void loadGraph(File graphFile) {
        GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(graphFile);
        Runtime.getRuntime().addShutdownHook(new Thread(graph::shutdown));
        Settings.setCurrentGraph(graph);
    }

    public Rephetio(File config) {
        JSONObject args = IO.buildJSONObject(config);
        Settings.home =  new File(config.getParent());
        File posFile = new File(Settings.home, "data/positives.txt");
        File negFile = new File(Settings.home, "data/negatives.txt");
        File graphFile = new File(Settings.home, "databases/graph.db");

        Settings.identifier = args.getString("identifier");
        Settings.target = args.getString("target");
        Settings.threads = args.getInt("threads");
        Settings.depth = args.getInt("depth");
        Settings.rw = args.getInt("rw");
        Settings.allowInverse = args.getBoolean("inverse");

        outFile = new File(Settings.home, args.getString("out"));
        if(!outFile.exists())
            outFile.mkdir();

        Settings.report();

        triples = new TripleSet(posFile, negFile);
        System.out.println("# Read " + triples.length() + " triples.");

        graph = new GraphDatabaseFactory().newEmbeddedDatabase(graphFile);
        Runtime.getRuntime().addShutdownHook(new Thread(graph::shutdown));
        Settings.setCurrentGraph(graph);

        try(Transaction tx = graph.beginTx()) {
            graph.getAllNodes().forEach( node -> nodeIndex.put(String.valueOf(node.getProperty(Settings.identifier)), node.getId()));
            tx.success();
        }
        System.out.println("# Finished Indexing nodes by identifiers.");

        startDepth = Math.floorDiv(Settings.depth, 2);
        endDepth = Settings.depth - startDepth;
    }

    public Table<Triple, MetaPath, Double> buildFeatureMatrix() {
        long s = System.currentTimeMillis();

        Table<Triple, MetaPath, Double> globalTable = HashBasedTable.create();
        List<Triple> tripleList = new ArrayList<>(triples.getTripleByPred(Settings.target));

        BlockingQueue<Triple> queue = new LinkedBlockingDeque<>(tripleList);
        TraverseTask[] tasks = new TraverseTask[Settings.threads];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = new TraverseTask(i, queue);
        }
        try {
            for (TraverseTask task : tasks) {
                task.join();
                globalTable.putAll(task.localTable);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("# Finished Building Matrix: Time = " + (float) (System.currentTimeMillis() - s) / 1000. + "s");

        IO.writeMatrix(globalTable, outFile);
        return globalTable;
    }

    class TraverseTask extends Thread {
        BlockingQueue<Triple> queue;
        Table<Triple, MetaPath, Double> localTable = HashBasedTable.create();

        public TraverseTask(int id, BlockingQueue<Triple> queue) {
            super("TraverseTask-" + id);
            this.queue = queue;
            start();
        }

        @Override
        public void run() {
            try(Transaction tx = graph.beginTx()) {
                while (!queue.isEmpty()) {
                    Triple triple = queue.poll();
                    if (triple != null) {
                        Long subId = nodeIndex.get(triple.sub);
                        Long objId = nodeIndex.get(triple.obj);
                        if(subId == null || objId == null) {
                            System.out.println("WARNING: Found Instance with unknown entities");
                            continue;
                        }

                        Node startNode = graph.getNodeById(subId);
                        Node endNode = graph.getNodeById(objId);

                        Traverser traverser = graph.bidirectionalTraversalDescription().startSide(
                                graph.traversalDescription().
                                        expand(standardRandomWalker(Settings.rw)).
                                        evaluator(Evaluators.toDepth(startDepth)).
                                        uniqueness(Uniqueness.NODE_PATH).
                                        depthFirst()
                        ).endSide(
                                graph.traversalDescription().
                                        expand(standardRandomWalker(Settings.rw)).
                                        evaluator(Evaluators.toDepth(endDepth)).
                                        uniqueness(Uniqueness.NODE_PATH).
                                        depthFirst()
                        ).collisionEvaluator((path -> {
                            if(path.length() == 1 && path.lastRelationship().getType().name().equals(triple.pred))
                                return Evaluation.EXCLUDE_AND_PRUNE;
                            return Evaluation.INCLUDE_AND_CONTINUE;
                        })).traverse(startNode, endNode);

                        List<Path> paths = new ArrayList<>();
                        for (Path path : traverser) {
                            paths.add(path);
                            MetaPath metaPath = new MetaPath(path);
                            double pdp = Quality.PDP(path, 0.4);
                            if(!localTable.contains(triple, metaPath))
                                localTable.put(triple,metaPath, pdp);
                            else
                                localTable.put(triple,metaPath, localTable.get(triple,metaPath) + pdp);
                        }

                        if (paths.isEmpty()) {
                            List<MetaPath> columnKeyList = ImmutableList.copyOf(localTable.columnKeySet());
                            for (MetaPath metaPath : columnKeyList) {
                                localTable.put(triple, metaPath, 0.);
                            }
                        }
                    }
                    Settings.updateProcessed();
                }
                tx.success();
            }
        }
    }

    public static PathExpander standardRandomWalker(int randomWalkers) {
        return new PathExpander() {
            @Override
            public Iterable<Relationship> expand(Path path, BranchState state) {
                Set<Relationship> results = Sets.newHashSet();
                List<Relationship> candidates = Lists.newArrayList( path.endNode().getRelationships() );
                if ( candidates.size() < randomWalkers || randomWalkers == 0 ) return candidates;

                Random rand = new Random();
                for ( int i = 0; i < randomWalkers; i++ ) {
                    int choice = rand.nextInt( candidates.size() );
                    results.add( candidates.get( choice ) );
                    candidates.remove( choice );
                }

                return results;
            }

            @Override
            public PathExpander reverse() {
                return null;
            }
        };
    }

}
