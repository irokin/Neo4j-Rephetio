package uk.ac.ncl.structs;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TripleSet {

    public Multimap<String, Triple> predMap = MultimapBuilder.hashKeys().hashSetValues().build();
    public Multimap<String, Triple> headMap = MultimapBuilder.hashKeys().hashSetValues().build();
    public Multimap<String, Triple> tailMap = MultimapBuilder.hashKeys().hashSetValues().build();

    public TripleSet(File file, boolean label) {
        try(LineIterator l = FileUtils.lineIterator(file)) {
            while(l.hasNext()) {
                String[] words = l.next().split("\t");
                assert words.length == 3;
                Triple triple = new Triple(words[0], words[1], words[2], label);
                predMap.put(triple.pred, triple);
                headMap.put(triple.sub, triple);
                tailMap.put(triple.obj, triple);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public TripleSet(File pos, File neg) {
        try(LineIterator l = FileUtils.lineIterator(pos)) {
            while(l.hasNext()) {
                String[] words = l.next().split("\t");
                assert words.length == 3;
                Triple triple = new Triple(words[0], words[1], words[2], true);
                predMap.put(triple.pred, triple);
                headMap.put(triple.sub, triple);
                tailMap.put(triple.obj, triple);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        try(LineIterator l = FileUtils.lineIterator(neg)) {
            while(l.hasNext()) {
                String[] words = l.next().split("\t");
                assert words.length == 3;
                Triple triple = new Triple(words[0], words[1], words[2], false);
                predMap.put(triple.pred, triple);
                headMap.put(triple.sub, triple);
                tailMap.put(triple.obj, triple);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public Set<Triple> getTripleByPred(String pred) {
        return new HashSet<>(predMap.get(pred));
    }

    public int length() {
        return predMap.values().size();
    }

}
