package malte0811.controlengineering.util.serialization;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Effectively RecordCodecBuilder, but provides access to the individual fields in a sane manner
 */
public class ListBasedCodec<T> implements Codec<T> {
    private final Codec<T> baseCodec;
    private final List<Pair<String, Codec<?>>> fields;

    private ListBasedCodec(Codec<T> baseCodec, List<Pair<String, Codec<?>>> fields) {
        this.baseCodec = baseCodec;
        this.fields = fields;
    }

    public static <T, T1, T2> ListBasedCodec<T> create(
            String name1, Codec<T1> codec1, Function<T, T1> get1,
            String name2, Codec<T2> codec2, Function<T, T2> get2,
            BiFunction<T1, T2, T> make
    ) {
        return new ListBasedCodec<>(
                RecordCodecBuilder.create(
                        inst -> inst.group(
                                codec1.fieldOf(name1).forGetter(get1),
                                codec2.fieldOf(name2).forGetter(get2)
                        ).apply(inst, make)
                ),
                ImmutableList.of(Pair.of(name1, codec1), Pair.of(name2, codec2))
        );
    }

    @Override
    public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
        return baseCodec.decode(ops, input);
    }

    @Override
    public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
        return baseCodec.encode(input, ops, prefix);
    }

    public List<Pair<String, Codec<?>>> getFields() {
        return fields;
    }
}
