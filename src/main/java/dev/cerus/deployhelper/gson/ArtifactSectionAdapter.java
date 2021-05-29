package dev.cerus.deployhelper.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.cerus.deployhelper.configuration.Config;
import java.io.IOException;

public class ArtifactSectionAdapter extends TypeAdapter<Config.ArtifactSection> {

    @Override
    public void write(final JsonWriter jsonWriter, final Config.ArtifactSection artifactSection) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("directory")
                .value(artifactSection.nameRegex());
        jsonWriter.name("name")
                .value(artifactSection.nameRegex());
        jsonWriter.name("sort")
                .value(artifactSection.sort().name());
        jsonWriter.endObject();
    }

    @Override
    public Config.ArtifactSection read(final JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();

        String directory = null;
        String name = null;
        Config.ArtifactSection.Sort sort = null;
        while (jsonReader.peek() == JsonToken.NAME) {
            switch (jsonReader.nextName().toLowerCase()) {
                case "directory" -> directory = jsonReader.nextString();
                case "name" -> name = jsonReader.nextString();
                case "sort" -> sort = Config.ArtifactSection.Sort.valueOf(jsonReader.nextString());
            }
        }

        jsonReader.endObject();
        return new Config.ArtifactSection(directory, name, sort);
    }

}
