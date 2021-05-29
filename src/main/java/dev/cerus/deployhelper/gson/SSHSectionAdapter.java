package dev.cerus.deployhelper.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.cerus.deployhelper.configuration.Config;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SSHSectionAdapter extends TypeAdapter<Config.SSHSection> {

    @Override
    public void write(final JsonWriter jsonWriter, final Config.SSHSection sshSection) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("port")
                .value(sshSection.port());
        jsonWriter.name("user")
                .value(sshSection.user());
        if (sshSection.password() != null) {
            jsonWriter.name("password")
                    .value(sshSection.password());
        }
        if (sshSection.keyPath() != null) {
            jsonWriter.name("key")
                    .value(sshSection.keyPath());
        }
        jsonWriter.endObject();
    }

    @Override
    public Config.SSHSection read(final JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();

        Integer port = null;
        String user = null;
        String password = null;
        String keyPath = null;
        long sleep = 5000;
        final List<String> commands = new ArrayList<>();
        while (jsonReader.peek() == JsonToken.NAME) {
            final String s = jsonReader.nextName();
            switch (s.toLowerCase()) {
                case "port" -> port = jsonReader.nextInt();
                case "user" -> user = jsonReader.nextString();
                case "password" -> password = jsonReader.nextString();
                case "key" -> keyPath = jsonReader.nextString();
                case "sleep" -> sleep = jsonReader.nextLong();
                case "commands" -> {
                    jsonReader.beginArray();
                    while (jsonReader.peek() == JsonToken.STRING) {
                        commands.add(jsonReader.nextString());
                    }
                    jsonReader.endArray();
                }
            }
        }

        jsonReader.endObject();
        return new Config.SSHSection(port, user, password, keyPath, sleep, commands);
    }

}
