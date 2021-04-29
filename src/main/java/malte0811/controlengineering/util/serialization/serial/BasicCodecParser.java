package malte0811.controlengineering.util.serialization.serial;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

public class BasicCodecParser<T> extends StringCodecParser<T> {
    public static final Codec<Integer> HEX_INT = Codec.INT.xmap(Function.identity(), Function.identity());

    public static final Map<Codec<?>, BasicCodecParser<?>> TO_PARSER = Util.make(
            ImmutableMap.<Codec<?>, BasicCodecParser<?>>builder(),
            builder -> {
                unthrow(Codec.INT, Integer::parseInt, i -> Integer.toString(i), builder);
                unthrow(HEX_INT, s -> Integer.parseInt(s, 16), i -> Integer.toString(i, 16), builder);
                unthrow(Codec.BOOL, Boolean::parseBoolean, b -> Boolean.toString(b), builder);
                register(Codec.STRING, DataResult::success, Function.identity(), builder);
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
    private final Function<T, String> stringify;

    private BasicCodecParser(
            Codec<T> codec, Function<String, DataResult<T>> parse, Function<T, String> stringify
    ) {
        super(codec);
        this.parse = parse;
        this.stringify = stringify;
    }

    public static <T> void register(
            Codec<T> base,
            Function<String, DataResult<T>> parse,
            Function<T, String> stringify,
            ImmutableMap.Builder<Codec<?>, BasicCodecParser<?>> out
    ) {
        BasicCodecParser<?> parser = new BasicCodecParser<>(base, parse, stringify);
        out.put(base, parser);
    }

    public static <T> void unthrow(
            Codec<T> base,
            ThrowingFunction<String, T, ?> parse,
            Function<T, String> stringify,
            ImmutableMap.Builder<Codec<?>, BasicCodecParser<?>> out
    ) {
        register(base, xcpToError(parse), stringify, out);
    }

    @Override
    protected DataResult<JsonElement> toJson(Queue<String> parts) {
        if (parts.isEmpty()) {
            return DataResult.error("Not enough data");
        }
        return parse.apply(parts.poll())
                .flatMap(t -> baseCodec.encodeStart(JsonOps.INSTANCE, t));
    }

    @Override
    protected void addTo(T in, List<String> parts) {
        parts.add(stringify.apply(in));
    }

    private interface ThrowingFunction<A, B, E extends Exception> {
        B apply(A obj) throws E;
    }
}
