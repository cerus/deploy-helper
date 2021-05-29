package dev.cerus.deployhelper.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.cerus.deployhelper.configuration.Config;
import java.io.IOException;

public class DestinationAdapter extends TypeAdapter<Config.Destination> {

    @Override
    public void write(final JsonWriter jsonWriter, final Config.Destination destination) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("name")
                .value(destination.name());
        jsonWriter.name("address")
                .value(destination.address());
        jsonWriter.name("path")
                .value(destination.path());
        jsonWriter.name("order")
                .value(destination.order().name());
        jsonWriter.endObject();
    }

    @Override
    public Config.Destination read(final JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();

        String name = null;
        String address = null;
        String path = null;
        Config.Destination.Order order = null;
        while (jsonReader.peek() == JsonToken.NAME) {
            switch (jsonReader.nextName().toLowerCase()) {
                case "name" -> name = jsonReader.nextString();
                case "address" -> address = jsonReader.nextString();
                case "path" -> path = jsonReader.nextString();
                case "order" -> order = Config.Destination.Order.valueOf(jsonReader.nextString());
            }
        }

        jsonReader.endObject();
        return new Config.Destination(name, address, path, order);
    }

}
