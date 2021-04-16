package malte0811.controlengineering.util.serialization.serial;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Util;

import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

public class BasicCodecParser<T> extends StringCodecParser<T> {
    public static final Codec<Integer> HEX_INT = Codec.INT.xmap(Function.identity(), Function.identity());

    public static final Map<Codec<?>, BasicCodecParser<?>> TO_PARSER = Util.make(
            ImmutableMap.<Codec<?>, BasicCodecParser<?>>builder(),
            builder -> {
                unthrow(Codec.INT, Integer::parseInt, builder);
                unthrow(HEX_INT, s -> Integer.parseInt(s, 16), builder);
                unthrow(Codec.BOOL, Boolean::parseBoolean, builder);
                register(Codec.STRING, DataResult::success, builder);
            }
    ).build();

    private static <A, B> Function<A, DataResult<B>> xcpToError(ThrowingFunction<A, B, ?> throwing) {
        return a -> {
            try {
                return DataResult.success(throwing.apply(a));
            } catch (Exception e) {
                return DataResult.error(e.getMessage());
            }
        };
    }

    private final Function<String, DataResult<T>> parse;

    private BasicCodecParser(Codec<T> codec, Function<String, DataResult<T>> parse) {
        super(codec);
        this.parse = parse;
    }

    public static <T> void register(
            Codec<T> base,
            Function<String, DataResult<T>> parse,
            ImmutableMap.Builder<Codec<?>, BasicCodecParser<?>> out
    ) {
        BasicCodecParser<?> parser = new BasicCodecParser<>(base, parse);
        out.put(base, parser);
    }

    public static <T> void unthrow(
            Codec<T> base,
            ThrowingFunction<String, T, ?> parse,
            ImmutableMap.Builder<Codec<?>, BasicCodecParser<?>> out
    ) {
        register(base, xcpToError(parse), out);
    }

    @Override
    protected DataResult<JsonElement> toJson(Queue<String> parts) {
        if (parts.isEmpty()) {
            return DataResult.error("Not enough data");
        }
        return parse.apply(parts.poll())
                .flatMap(t -> baseCodec.encodeStart(JsonOps.INSTANCE, t));
    }

    private interface ThrowingFunction<A, B, E extends Exception> {
        B apply(A obj) throws E;
    }
}
