package dev.cerus.deployhelper.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.cerus.deployhelper.configuration.Config;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigAdapter extends TypeAdapter<Config> {

    private final DestinationAdapter destinationAdapter = new DestinationAdapter();
    private final SSHSectionAdapter sshSectionAdapter = new SSHSectionAdapter();
    private final CommandsSectionAdapter commandsSectionAdapter = new CommandsSectionAdapter();
    private final ArtifactSectionAdapter artifactSectionAdapter = new ArtifactSectionAdapter();

    @Override
    public void write(final JsonWriter jsonWriter, final Config config) throws IOException {
        jsonWriter.beginObject();

        if (config.destinations() != null && config.destinations().size() > 0) {
            jsonWriter.name("destinations");
            jsonWriter.beginArray();

            for (final Config.Destination destination : config.destinations()) {
                this.destinationAdapter.write(jsonWriter, destination);
            }

            jsonWriter.endArray();
        }

        if (config.ssh() != null) {
            jsonWriter.name("ssh");
            this.sshSectionAdapter.write(jsonWriter, config.ssh());
        }

        if (config.commands() != null) {
            jsonWriter.name("commands");
            this.commandsSectionAdapter.write(jsonWriter, config.commands());
        }

        if (config.artifact() != null) {
            jsonWriter.name("artifact");
            this.artifactSectionAdapter.write(jsonWriter, config.artifact());
        }

        jsonWriter.endObject();
    }

    @Override
    public Config read(final JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();

        final List<Config.Destination> destinations = new ArrayList<>();
        Config.SSHSection sshSection = null;
        Config.CommandsSection commandsSection = null;
        Config.ArtifactSection artifactSection = null;
        while (jsonReader.peek() == JsonToken.NAME) {
            final String s = jsonReader.nextName();
            switch (s.toLowerCase()) {
                case "destinations" -> {
                    jsonReader.beginArray();
                    while (jsonReader.peek() == JsonToken.BEGIN_OBJECT) {
                        destinations.add(this.destinationAdapter.read(jsonReader));
                    }
                    jsonReader.endArray();
                }
                case "ssh" -> sshSection = this.sshSectionAdapter.read(jsonReader);
                case "commands" -> commandsSection = this.commandsSectionAdapter.read(jsonReader);
                case "artifact" -> artifactSection = this.artifactSectionAdapter.read(jsonReader);
            }
        }

        jsonReader.endObject();
        return new Config(destinations, sshSection, commandsSection, artifactSection);
    }
}
