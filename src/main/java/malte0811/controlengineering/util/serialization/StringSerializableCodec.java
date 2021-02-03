package malte0811.controlengineering.util.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.INBT;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StringSerializableCodec<T> {
    // TODO Optional -> DataResult?
    private final Function<List<String>, DataResult<T>> fromString;
    private final Function<INBT, DataResult<T>> fromNBT;
    private final Function<T, INBT> toNBT;
    private final int consumedTokens;

    public StringSerializableCodec(
            Function<List<String>, DataResult<T>> fromString, Function<INBT, DataResult<T>> fromNBT,
            Function<T, INBT> toNBT, int consumedTokens
    ) {
        this.fromString = fromString;
        this.fromNBT = fromNBT;
        this.toNBT = toNBT;
        this.consumedTokens = consumedTokens;
    }

    public static <T> StringSerializableCodec<T> fromCodec(
            Codec<T> pureCodec, Function<List<String>, DataResult<T>> fromString, int numConsumed
    ) {
        return new StringSerializableCodec<>(
                fromString, nbt -> Codecs.read(pureCodec, nbt), t -> Codecs.encode(pureCodec, t), numConsumed
        );
    }

    public static <T> StringSerializableCodec<T> fromCodec(
            Codec<T> pureCodec,
            Function<String, DataResult<T>> fromString
    ) {
        return fromCodec(pureCodec, s -> fromString.apply(s.get(0)), 1);
    }

    public static <T> StringSerializableCodec<T> fromCodecXcpError(Codec<T> pureCodec, Function<String, T> fromString) {
        return fromCodec(pureCodec, wrapXcp(fromString));
    }

    public static <T> StringSerializableCodec<T> fromCodec(
            Codec<T> pureCodec, BiFunction<String, String, DataResult<T>> fromString
    ) {
        return fromCodec(pureCodec, s -> fromString.apply(s.get(0), s.get(1)), 2);
    }

    public static <T> StringSerializableCodec<T> fromCodecXcpError(
            Codec<T> pureCodec, BiFunction<String, String, T> fromString
    ) {
        return fromCodec(pureCodec, wrapXcp(s -> fromString.apply(s.get(0), s.get(1))), 2);
    }

    public DataResult<T> fromNBT(INBT in) {
        return fromNBT.apply(in);
    }

    public DataResult<T> fromString(List<String> in) {
        return fromString.apply(in);
    }

    public int numTokens() {
        return consumedTokens;
    }

    public INBT toNBT(T in) {
        return toNBT.apply(in);
    }

    public StringSerializableCodec<T> constant(T i) {
        return new StringSerializableCodec<>(s -> DataResult.success(i), fromNBT, toNBT, 0);
    }

    private static <A, B> Function<A, DataResult<B>> wrapXcp(Function<A, B> base) {
        return a -> {
            try {
                return DataResult.success(base.apply(a));
            } catch (NumberFormatException x) {
                return DataResult.error("Invalid number: " + x.getMessage());
            } catch (Exception x) {
                x.printStackTrace();
                return DataResult.error("Unexpected error: " + x.getMessage());
            }
        };
    }
}
