package malte0811.controlengineering.util.serialization.serial;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import malte0811.controlengineering.util.serialization.ListBasedCodec;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class ListCodecParser<T> extends StringCodecParser<T> {
    private final List<Field<?>> fields;

    public ListCodecParser(ListBasedCodec<T> codec) {
        super(codec);
        fields = codec.getFields().stream()
                .map(p -> new Field<>(p.getFirst(), p.getSecond()))
                .collect(Collectors.toList());
    }

    @Override
    protected DataResult<JsonElement> toJson(Queue<String> parts) {
        JsonObject result = new JsonObject();
        for (Field<?> f : fields) {
            DataResult<JsonElement> fieldValue = f.parser.toJson(parts);
            if (fieldValue.result().isPresent()) {
                result.add(f.name, fieldValue.result().get());
            } else {
                Preconditions.checkState(fieldValue.error().isPresent());
                return fieldValue.mapError(s -> f.name + ": " + s);
            }
        }
        return DataResult.success(result);
    }

    private static class Field<T> {
        private final String name;
        private final StringCodecParser<T> parser;

        private Field(String name, Codec<T> codec) {
            this.name = name;
            this.parser = StringCodecParser.getParser(codec);
        }
    }
}
