package malte0811.controlengineering.util.serialization;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.Codecs;
import net.minecraft.nbt.INBT;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StringSerializableCodec<T> {
    private final Function<List<String>, T> fromString;
    private final Function<INBT, T> fromNBT;
    private final Function<T, INBT> toNBT;
    private final int consumedTokens;

    public StringSerializableCodec(
            Function<List<String>, T> fromString, Function<INBT, T> fromNBT, Function<T, INBT> toNBT, int consumedTokens
    ) {
        this.fromString = fromString;
        this.fromNBT = fromNBT;
        this.toNBT = toNBT;
        this.consumedTokens = consumedTokens;
    }

    public static <T> StringSerializableCodec<T> fromCodec(Codec<T> pureCodec, Function<String, T> fromString) {
        return new StringSerializableCodec<>(
                s -> fromString.apply(s.get(0)),
                nbt -> Codecs.read(pureCodec, nbt),
                t -> Codecs.encode(pureCodec, t),
                1
        );
    }

    public static <T> StringSerializableCodec<T> fromCodec(
            Codec<T> pureCodec,
            BiFunction<String, String, T> fromString
    ) {
        return new StringSerializableCodec<>(
                s -> fromString.apply(s.get(0), s.get(1)),
                nbt -> Codecs.read(pureCodec, nbt),
                t -> Codecs.encode(pureCodec, t),
                2
        );
    }

    public T fromNBT(INBT in) {
        return fromNBT.apply(in);
    }

    public T fromString(List<String> in) throws Exception {
        return fromString.apply(in);
    }

    public int numTokens() {
        return consumedTokens;
    }

    public INBT toNBT(T in) {
        return toNBT.apply(in);
    }

    public StringSerializableCodec<T> constant(T i) {
        return new StringSerializableCodec<>(s -> i, fromNBT, toNBT, 0);
    }
}
