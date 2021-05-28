package de.cerus.deployhelper.configuration;

import java.util.List;

public record Config(List<Destination> destinations, SSHSection ssh, CommandsSection commands, ArtifactSection artifact) {

    public static record Destination(String name, String address, String path, Order order) {

        public enum Order {
            COMMANDS_SSH,
            SSH_COMMANDS
        }

    }

    public static record ArtifactSection(String directory, String nameRegex, Sort sort) {

        public enum Sort {
            LAST_MODIFIED_ASC,
            LAST_MODIFIED_DESC,
        }

    }

    public static record CommandsSection(List<String[]> commands) {
    }

    public static record SSHSection(Integer port, String user, String password, String keyPath, long sleep, List<String> commands) {
    }

}
