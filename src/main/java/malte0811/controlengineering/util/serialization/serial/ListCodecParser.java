package malte0811.controlengineering.util.serialization.serial;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import malte0811.controlengineering.util.serialization.ListBasedCodec;

import java.util.List;
import java.util.stream.Collectors;

public class ListCodecParser<T> extends SerialCodecParser<T> {
    private final List<ParserField<T, ?>> fields;

    public ListCodecParser(ListBasedCodec<T> codec) {
        super(codec);
        fields = codec.getFields().stream()
                .map(ParserField::new)
                .collect(Collectors.toList());
    }

    @Override
    protected DataResult<JsonElement> toJson(SerialStorage parts) {
        JsonObject result = new JsonObject();
        for (ParserField<T, ?> f : fields) {
            DataResult<JsonElement> fieldValue = f.parser.toJson(parts);
            if (fieldValue.result().isPresent()) {
                result.add(f.field.name(), fieldValue.result().get());
            } else {
                Preconditions.checkState(fieldValue.error().isPresent());
                return fieldValue.mapError(s -> f.field.name() + ": " + s);
            }
        }
        return DataResult.success(result);
    }

    @Override
    public void addTo(T in, SerialStorage parts) {
        for (ParserField<T, ?> f : fields) {
            f.addTo(in, parts);
        }
    }

    private static class ParserField<T, T1> {
        private final ListBasedCodec.Field<T, T1> field;
        private final SerialCodecParser<T1> parser;

        private ParserField(ListBasedCodec.Field<T, T1> field) {
            this.field = field;
            this.parser = SerialCodecParser.getParser(field.fieldCodec());
        }

        private void addTo(T in, SerialStorage parts) {
            parser.addTo(field.getter().apply(in), parts);
        }
    }
}
