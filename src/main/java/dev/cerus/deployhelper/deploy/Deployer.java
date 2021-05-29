package dev.cerus.deployhelper.deploy;

import dev.cerus.deployhelper.Launcher;
import dev.cerus.deployhelper.configuration.Config;
import dev.cerus.deployhelper.util.TriFunction;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Deployer {

    private final List<TriFunction<String, Config.Destination, File, String>> variableReplacements;
    private final Config config;
    private final Launcher.Options options;

    public Deployer(final Config config, final Launcher.Options options) {
        this.config = config;
        this.options = options;

        this.variableReplacements = Arrays.asList(
                (s, destination, artifact) -> s.replace("{DEST_ADDRESS}", destination.address()),
                (s, destination, artifact) -> s.replace("{DEST_NAME}", destination.name()),
                (s, destination, artifact) -> s.replace("{DEST_PATH}", destination.path()),
                (s, destination, artifact) -> s.replace("{SSH_USER}", config.ssh().user()),
                (s, destination, artifact) -> s.replace("{SSH_PORT}", String.valueOf(config.ssh().port())),
                (s, destination, artifact) -> s.replace("{ARTIFACT_PATH}", artifact.getAbsolutePath()),
                (s, destination, artifact) -> s.replace("{ARTIFACT_NAME}", artifact.getName())
        );
    }

    /**
     * Runs the configured deployment tasks for the specified destinations
     */
    public void deploy() {
        // Map destinations for easier lookup and find artifact
        final Map<String, Config.Destination> destinationMap = this.config.destinations().stream()
                .collect(Collectors.toMap(Config.Destination::name, destination -> destination));
        final File artifact = this.findArtifact();

        // Check arguments
        if (this.options.destination != null && this.options.destination.length() > 0) {
            // Loop through the specified destinations and run deploy tasks
            for (final String destName : this.options.destination.split(",")) {
                final Config.Destination destination = destinationMap.get(destName);
                if (destination != null) {
                    // Deploy
                    this.deployTo(destination, artifact);
                } else {
                    // Show warning
                    System.err.println("Unknown destination " + destName);
                }
            }
        } else {
            // Deploy to first destination in list
            final Config.Destination destination = this.config.destinations().get(0);
            this.deployTo(destination, artifact);
        }
    }

    /**
     * Deploys to the destination
     * Purpose of this method is to run the tasks in the correct order.
     *
     * @param destination The destination
     * @param artifact    The artifact to deploy
     */
    private void deployTo(final Config.Destination destination, final File artifact) {
        System.out.println("Deploying to " + destination.name());
        switch (destination.order()) {
            case COMMANDS_SSH -> {
                this.execCommands(destination, artifact);
                this.execSsh(destination);
            }
            case SSH_COMMANDS -> {
                this.execSsh(destination);
                this.execCommands(destination, artifact);
            }
        }
    }

    /**
     * Executes local commands
     *
     * @param destination The destination
     * @param artifact    The artifact to deploy
     */
    private void execCommands(final Config.Destination destination, final File artifact) {
        if (this.config.commands() == null) {
            return;
        }

        for (final String[] rawCommand : this.config.commands().commands()) {
            // Prepare command (replace variables)
            final String[] command = this.prepareCommand(rawCommand, destination, artifact);
            System.out.println("Running " + Arrays.toString(command));

            // Build process
            final ProcessBuilder builder = new ProcessBuilder(command)
                    .directory(new File("."));
            if (this.options.verbose) {
                builder.inheritIO()
                        .redirectInput(ProcessBuilder.Redirect.PIPE);
            }

            // Spawn process
            try {
                builder.start().waitFor();
            } catch (final InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Spawns a ssh process and inputs the configured commands
     * OpenSSH (or a equivalent) has to be installed for this to work. For password based authentication 'sshpass' is required.
     *
     * @param destination The destination
     */
    private void execSsh(final Config.Destination destination) {
        if (this.config.ssh() == null) {
            return;
        }

        // Build ssh command
        final boolean usePass = this.config.ssh().password() != null;
        final List<String> tempList = new ArrayList<>(Arrays.asList(
                "ssh",
                this.config.ssh().user() + "@" + destination.address() + (this.config.ssh().port() <= 0 ? "" : ":" + this.config.ssh().port())
        ));
        if (usePass) {
            // Use plaintext password
            tempList.add(0, "-p");
            tempList.add(0, "sshpass");
        }
        final String[] command = tempList.toArray(new String[0]);

        // Build ssh process
        final ProcessBuilder builder = new ProcessBuilder(command)
                .directory(new File("."));
        if (this.options.verbose) {
            builder.inheritIO()
                    .redirectInput(ProcessBuilder.Redirect.PIPE);
        }

        try {
            // Spawn ssh process
            final Process process = builder.start();

            // Wait for the connection
            // This is actually a pretty shitty implementation
            Thread.sleep(this.config.ssh().sleep());

            // Get output stream and change directory
            final OutputStream outputStream = process.getOutputStream();
            outputStream.write(("cd " + destination.path() + "\n").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            // Run the commands
            for (final String cmd : this.config.ssh().commands()) {
                outputStream.write((cmd + "\n").getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }

            // Exit the shell
            outputStream.write("exit\n".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            // Wait for the process to end
            process.waitFor();
        } catch (final InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Replaces variables in a command array
     *
     * @param command     The command array
     * @param destination The destination
     * @param artifact    The artifact to deploy
     *
     * @return A command array with replaced variables
     */
    private String[] prepareCommand(final String[] command, final Config.Destination destination, final File artifact) {
        // Make a copy of the array
        final String[] copy = new String[command.length];
        System.arraycopy(command, 0, copy, 0, command.length);

        // Run replacements for every single element
        for (int i = 0; i < copy.length; i++) {
            String str = copy[i];
            for (final TriFunction<String, Config.Destination, File, String> fun : this.variableReplacements) {
                str = fun.apply(str, destination, artifact);
            }
            copy[i] = str;
        }
        return copy;
    }

    /**
     * Attempts to find the artifact based on the configured rules
     *
     * @return The artifact or null
     */
    private File findArtifact() {
        final Config.ArtifactSection artifactSection = this.config.artifact();
        final File dir = new File(artifactSection.directory());

        return Arrays.stream(dir.listFiles())
                .filter(Objects::nonNull) // Filter out null values
                .filter(file -> file.getName().matches(artifactSection.nameRegex())) // Check if file name matches
                .min(switch (artifactSection.sort()) { // Sort
                    case LAST_MODIFIED_ASC -> Comparator.comparingLong(File::lastModified);
                    case LAST_MODIFIED_DESC -> Comparator.comparingLong(File::lastModified).reversed();
                })
                .orElse(null);
    }

}
