package uk.ac.ncl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import uk.ac.ncl.model.Rephetio;
import uk.ac.ncl.structs.MetaPath;
import uk.ac.ncl.utils.Logging;

import java.io.File;
import java.util.Set;

public class Run {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("c").hasArg().desc("Specify the location of config file.").build());
        options.addOption(Option.builder("cm").desc("Create candidate matrix.").build());

        try {
            CommandLine cmd = (new DefaultParser()).parse(options, args);

            if(cmd.hasOption("c")) {
                File config = new File(cmd.getOptionValue("c"));
                Rephetio system = new Rephetio(config);
                Set<MetaPath> metaPaths = system.buildFeatureMatrix();
                if(cmd.hasOption("cm"))
                    system.buildCandidateMatrix(metaPaths);
                Logging.report();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
