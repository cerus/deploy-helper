package dev.cerus.deployhelper;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;
import com.google.devtools.common.options.OptionsParser;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import dev.cerus.deployhelper.configuration.Config;
import dev.cerus.deployhelper.deploy.Deployer;
import dev.cerus.deployhelper.gson.ArtifactSectionAdapter;
import dev.cerus.deployhelper.gson.CommandsSectionAdapter;
import dev.cerus.deployhelper.gson.ConfigAdapter;
import dev.cerus.deployhelper.gson.DestinationAdapter;
import dev.cerus.deployhelper.gson.SSHSectionAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.Collections;

public class Launcher {

    public static void main(final String[] args) {
        final OptionsParser parser = OptionsParser.newOptionsParser(Options.class);
        parser.parseAndExitUponError(args);
        final Options options = parser.getOptions(Options.class);

        final File configFile = new File(options.configPath);
        if (!configFile.exists()) {
            System.err.println("No " + options.configPath + " found");
            return;
        }

        final Config config;
        try {
            config = new GsonBuilder()
                    .registerTypeAdapter(Config.class, new ConfigAdapter())
                    .registerTypeAdapter(Config.Destination.class, new DestinationAdapter())
                    .registerTypeAdapter(Config.SSHSection.class, new SSHSectionAdapter())
                    .registerTypeAdapter(Config.CommandsSection.class, new CommandsSectionAdapter())
                    .registerTypeAdapter(Config.ArtifactSection.class, new ArtifactSectionAdapter())
                    .setLongSerializationPolicy(LongSerializationPolicy.STRING)
                    .setPrettyPrinting()
                    .create()
                    .fromJson(new FileReader(configFile), Config.class);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Failed to load config");
            return;
        }

        if (config.artifact() == null) {
            System.err.println("Artifact section not found");
            return;
        }
        if (config.destinations() == null) {
            System.err.println("Destination section not found");
            return;
        }
        if (config.destinations().isEmpty()) {
            System.err.println("Destination section is empty");
            return;
        }

        final Deployer deployer = new Deployer(config, options);
        deployer.deploy();
    }

    private static void fail(final OptionsParser parser) {
        File jar = new File("deploy-helper.jar");
        try {
            jar = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (final URISyntaxException ignored) {
        }

        System.out.println("Usage: java -jar " + jar.getName() + " OPTIONS");
        System.out.println(parser.describeOptions(Collections.emptyMap(), OptionsParser.HelpVerbosity.LONG));
    }

    public static class Options extends OptionsBase {

        @Option(
                name = "destination",
                abbrev = 'd',
                help = "Overrides the destinations",
                valueHelp = "destinations concatenated by a comma",
                defaultValue = ""
        )
        public String destination;

        @Option(
                name = "config",
                abbrev = 'c',
                help = "Overrides the config file",
                defaultValue = "deploy-helper.json"
        )
        public String configPath;

        @Option(
                name = "verbose",
                abbrev = 'v',
                help = "Shows individual process output",
                defaultValue = "false"
        )
        public boolean verbose;

    }

}
