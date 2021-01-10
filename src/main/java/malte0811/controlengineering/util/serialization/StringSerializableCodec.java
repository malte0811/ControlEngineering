package malte0811.controlengineering.util.serialization;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.INBT;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StringSerializableCodec<T> {
    // TODO Optional -> DataResult?
    private final Function<List<String>, Optional<T>> fromString;
    private final Function<INBT, Optional<T>> fromNBT;
    private final Function<T, INBT> toNBT;
    private final int consumedTokens;

    public StringSerializableCodec(
            Function<List<String>, Optional<T>> fromString, Function<INBT, Optional<T>> fromNBT,
            Function<T, INBT> toNBT, int consumedTokens
    ) {
        this.fromString = fromString;
        this.fromNBT = fromNBT;
        this.toNBT = toNBT;
        this.consumedTokens = consumedTokens;
    }

    public static <T> StringSerializableCodec<T> fromCodec(
            Codec<T> pureCodec, Function<List<String>, Optional<T>> fromString, int numConsumed
    ) {
        return new StringSerializableCodec<>(
                fromString, nbt -> Codecs.read(pureCodec, nbt), t -> Codecs.encode(pureCodec, t), numConsumed
        );
    }

    public static <T> StringSerializableCodec<T> fromCodec(
            Codec<T> pureCodec,
            Function<String, Optional<T>> fromString
    ) {
        return fromCodec(pureCodec, s -> fromString.apply(s.get(0)), 1);
    }

    public static <T> StringSerializableCodec<T> fromCodecXcpError(Codec<T> pureCodec, Function<String, T> fromString) {
        return fromCodec(pureCodec, wrapXcp(fromString));
    }

    public static <T> StringSerializableCodec<T> fromCodec(
            Codec<T> pureCodec, BiFunction<String, String, Optional<T>> fromString
    ) {
        return fromCodec(pureCodec, s -> fromString.apply(s.get(0), s.get(1)), 2);
    }

    public static <T> StringSerializableCodec<T> fromCodecXcpError(
            Codec<T> pureCodec, BiFunction<String, String, T> fromString
    ) {
        return fromCodec(pureCodec, wrapXcp(s -> fromString.apply(s.get(0), s.get(1))), 2);
    }

    public Optional<T> fromNBT(INBT in) {
        return fromNBT.apply(in);
    }

    public Optional<T> fromString(List<String> in) {
        return fromString.apply(in);
    }

    public int numTokens() {
        return consumedTokens;
    }

    public INBT toNBT(T in) {
        return toNBT.apply(in);
    }

    public StringSerializableCodec<T> constant(T i) {
        return new StringSerializableCodec<>(s -> Optional.of(i), fromNBT, toNBT, 0);
    }

    private static <A, B> Function<A, Optional<B>> wrapXcp(Function<A, B> base) {
        return a -> {
            try {
                return Optional.of(base.apply(a));
            } catch (Exception x) {
                return Optional.empty();
            }
        };
    }
}
