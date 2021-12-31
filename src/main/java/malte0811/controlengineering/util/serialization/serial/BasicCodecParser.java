package malte0811.controlengineering.util.serialization.serial;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import malte0811.controlengineering.util.FastDataResult;
import net.minecraft.Util;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BasicCodecParser<T> extends SerialCodecParser<T> {
    public static final Codec<Integer> HEX_INT = Codec.INT.xmap(Function.identity(), Function.identity());

    public static final Map<Codec<?>, BasicCodecParser<?>> TO_PARSER = Util.make(
            ImmutableMap.<Codec<?>, BasicCodecParser<?>>builder(),
            builder -> {
                register(Codec.INT, SerialStorage::readInt, SerialStorage::writeInt, builder);
                register(HEX_INT, SerialStorage::readHexInt, SerialStorage::writeHexInt, builder);
                register(Codec.BOOL, SerialStorage::readBoolean, SerialStorage::writeBoolean, builder);
                register(Codec.STRING, SerialStorage::readString, SerialStorage::writeString, builder);
            }
    ).build();

    private final Function<SerialStorage, FastDataResult<T>> parse;
    private final BiConsumer<SerialStorage, T> stringify;

    private BasicCodecParser(
            Codec<T> codec, Function<SerialStorage, FastDataResult<T>> parse, BiConsumer<SerialStorage, T> stringify
    ) {
        super(codec);
        this.parse = parse;
        this.stringify = stringify;
    }

    public static <T> void register(
            Codec<T> base,
            Function<SerialStorage, FastDataResult<T>> parse,
            BiConsumer<SerialStorage, T> stringify,
            ImmutableMap.Builder<Codec<?>, BasicCodecParser<?>> out
    ) {
        BasicCodecParser<?> parser = new BasicCodecParser<>(base, parse, stringify);
        out.put(base, parser);
    }

    @Override
    protected FastDataResult<JsonElement> toJson(SerialStorage parts) {
        return parse.apply(parts)
                .flatMapDFU(t -> baseCodec.encodeStart(JsonOps.INSTANCE, t));
    }

    @Override
    public void addTo(T in, SerialStorage parts) {
        stringify.accept(parts, in);
    }
}
