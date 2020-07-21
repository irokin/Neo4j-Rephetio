package uk.ac.ncl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import uk.ac.ncl.model.Rephetio;

import java.io.File;

public class Run {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("c").hasArg().desc("Specify the location of config file.").build());

        try {
            CommandLine cmd = (new DefaultParser()).parse(options, args);

            if(cmd.hasOption("c")) {
                File config = new File(cmd.getOptionValue("c"));
                Rephetio system = new Rephetio(config);
                system.buildFeatureMatrix();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
