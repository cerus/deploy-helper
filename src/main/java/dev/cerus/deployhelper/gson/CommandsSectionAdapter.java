package dev.cerus.deployhelper.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.cerus.deployhelper.configuration.Config;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandsSectionAdapter extends TypeAdapter<Config.CommandsSection> {

    @Override
    public void write(final JsonWriter jsonWriter, final Config.CommandsSection destination) throws IOException {
        jsonWriter.beginArray();
        for (final String[] command : destination.commands()) {
            jsonWriter.beginArray();
            for (final String s : command) {
                jsonWriter.value(s);
            }
            jsonWriter.endArray();
        }
        jsonWriter.endArray();
    }

    @Override
    public Config.CommandsSection read(final JsonReader jsonReader) throws IOException {
        jsonReader.beginArray();

        final List<String[]> commands = new ArrayList<>();
        while (jsonReader.peek() == JsonToken.STRING || jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            if (jsonReader.peek() == JsonToken.STRING) {
                commands.add(this.parseCommand(jsonReader.nextString()));
            } else {
                jsonReader.beginArray();
                final List<String> tempList = new ArrayList<>();
                while (jsonReader.peek() == JsonToken.STRING) {
                    tempList.add(jsonReader.nextString());
                }
                commands.add(tempList.toArray(new String[0]));
                jsonReader.endArray();
            }
        }

        jsonReader.endArray();
        return new Config.CommandsSection(commands);
    }

    private String[] parseCommand(final String command) {
        final char[] chars = command.toCharArray();
        final List<String> list = new ArrayList<>();
        StringBuffer buffer = new StringBuffer();
        boolean inStr = false;
        boolean escaped = false;

        for (final char c : chars) {
            if (c == '"' && !escaped) {
                inStr = !inStr;
            } else if (c == ' ' && !inStr && !buffer.isEmpty()) {
                list.add(buffer.toString());
                buffer = new StringBuffer();
            } else if (c != '\\') {
                buffer.append(c);
            }

            if (escaped) {
                escaped = false;
            }
            if (c == '\\') {
                escaped = true;
            }
        }
        if (!buffer.isEmpty()) {
            list.add(buffer.toString());
        }

        return list.toArray(new String[0]);
    }

}
