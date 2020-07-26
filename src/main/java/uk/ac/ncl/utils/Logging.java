package uk.ac.ncl.utils;

import java.text.MessageFormat;

public class Logging {

    public static int tripleWithNoPath = 0;

    public static void report() {
        System.out.println(MessageFormat.format("# TripleWithNoPath: {0}", tripleWithNoPath));
    }

    public static synchronized void updateTripleWithNoPath() {
        tripleWithNoPath++;
    }
}
