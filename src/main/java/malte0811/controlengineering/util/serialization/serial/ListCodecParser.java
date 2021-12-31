package malte0811.controlengineering.util.serialization.serial;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import malte0811.controlengineering.util.FastDataResult;
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
    protected FastDataResult<JsonElement> toJson(SerialStorage parts) {
        JsonObject result = new JsonObject();
        for (ParserField<T, ?> f : fields) {
            FastDataResult<JsonElement> fieldValue = f.parser.toJson(parts);
            if (fieldValue.isError()) {
                return FastDataResult.error(f.field.name() + ": " + fieldValue.getErrorMessage());
            } else {
                result.add(f.field.name(), fieldValue.get());
            }
        }
        return FastDataResult.success(result);
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
