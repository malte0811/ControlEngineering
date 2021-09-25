package malte0811.controlengineering.util.serialization.serial;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import malte0811.controlengineering.util.serialization.ListBasedCodec;
import net.minecraft.network.FriendlyByteBuf;
import java.util.List;

public abstract class SerialCodecParser<T> {
    public static <T> SerialCodecParser<T> getParser(Codec<T> codec) {
        SerialCodecParser<?> result = BasicCodecParser.TO_PARSER.get(codec);
        if (result == null && codec instanceof ListBasedCodec<?>) {
            result = new ListCodecParser<>((ListBasedCodec<?>) codec);
        }
        if (result == null) {
            throw new RuntimeException("No parser for codec " + codec + "!");
        }
        return (SerialCodecParser<T>) result;
    }

    protected final Codec<T> baseCodec;

    protected SerialCodecParser(Codec<T> baseCodec) {
        this.baseCodec = baseCodec;
    }

    public final DataResult<T> parse(List<String> parts) {
        return parse(new StringListStorage(parts));
    }

    public final DataResult<T> parse(FriendlyByteBuf parts) {
        return parse(new PacketBufferStorage(parts));
    }

    public final DataResult<T> parse(SerialStorage parts) {
        return toJson(parts).flatMap(json -> baseCodec.parse(JsonOps.INSTANCE, json));
    }

    public final List<String> stringify(T in) {
        StringListStorage out = new StringListStorage();
        addTo(in, out);
        return out.getData();
    }

    public final void writeToBuffer(T in, FriendlyByteBuf out) {
        addTo(in, new PacketBufferStorage(out));
    }

    protected abstract DataResult<JsonElement> toJson(SerialStorage parts);

    public abstract void addTo(T in, SerialStorage parts);
}
