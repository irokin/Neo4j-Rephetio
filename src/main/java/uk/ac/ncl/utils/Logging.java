package uk.ac.ncl.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

public class Logging {

    public static int tripleWithNoPath = 0;
    static File logFile;

    public static void init(File logFile) {
        Logging.logFile = logFile;
        try(PrintWriter writer = new PrintWriter(new FileWriter(logFile, false))) {
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void println(String s) {
        if(logFile == null) {
            Logging.println("LogFile not initialized.");
            System.exit(-1);
        } else {
            try(PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                writer.println(s);
                System.out.println(s);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public static void report() {
        if(logFile == null) {
            String msg = MessageFormat.format("# TripleWithNoPath: {0}", tripleWithNoPath);
            System.out.println(msg);
        } else {
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                String msg = MessageFormat.format("# TripleWithNoPath: {0}", tripleWithNoPath);
                writer.println(msg);
                System.out.println(msg);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public static synchronized void updateTripleWithNoPath() {
        tripleWithNoPath++;
    }
}
