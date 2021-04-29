package malte0811.controlengineering.util.serialization.serial;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import malte0811.controlengineering.util.serialization.ListBasedCodec;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

// TODO extend to PacketBuffer's
public abstract class StringCodecParser<T> {
    public static <T> StringCodecParser<T> getParser(Codec<T> codec) {
        StringCodecParser<?> result = BasicCodecParser.TO_PARSER.get(codec);
        if (result == null && codec instanceof ListBasedCodec<?>) {
            result = new ListCodecParser<>((ListBasedCodec<?>) codec);
        }
        if (result == null) {
            throw new RuntimeException("No parser for codec " + codec + "!");
        }
        return (StringCodecParser<T>) result;
    }

    protected final Codec<T> baseCodec;

    protected StringCodecParser(Codec<T> baseCodec) {
        this.baseCodec = baseCodec;
    }

    public final DataResult<T> parse(List<String> parts) {
        return toJson(new ArrayDeque<>(parts))
                .flatMap(json -> baseCodec.parse(JsonOps.INSTANCE, json));
    }

    public final List<String> stringify(T in) {
        List<String> result = new ArrayList<>();
        addTo(in, result);
        return result;
    }

    protected abstract DataResult<JsonElement> toJson(Queue<String> parts);

    protected abstract void addTo(T in, List<String> parts);
}
