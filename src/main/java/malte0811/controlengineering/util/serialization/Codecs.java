package malte0811.controlengineering.util.serialization;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.Direction;

import java.util.Optional;
import java.util.function.Function;

public class Codecs {
    public static final Codec<INBT> INBT_CODEC = Codec.PASSTHROUGH.flatXmap(
            dyn -> DataResult.success(dyn.convert(NBTDynamicOps.INSTANCE).getValue()),
            inbt -> DataResult.success(new Dynamic<>(NBTDynamicOps.INSTANCE, inbt))
    );

    public static final Codec<Direction> DIRECTION_CODEC = Codec.INT.xmap(
            i -> DirectionUtils.VALUES[i],
            Direction::ordinal
    );

    public static <T> Optional<T> read(Codec<T> codec, CompoundNBT in, String subName) {
        return read(codec, in.get(subName));
    }

    public static <T> Optional<T> read(Codec<T> codec, INBT nbt) {
        return codec.decode(NBTDynamicOps.INSTANCE, nbt)
                .result()
                .map(Pair::getFirst);
    }

    public static <T> void add(Codec<T> codec, T value, CompoundNBT out, String subName) {
        out.put(subName, encode(codec, value));
    }

    public static <T> INBT encode(Codec<T> codec, T value) {
        return codec.encodeStart(NBTDynamicOps.INSTANCE, value).getOrThrow(false, s -> {});
    }

    public static <T1, T2> Codec<T1> xmapDataResult(
            Codec<T2> baseCodec, Function<T1, T2> unpack, Function<T2, DataResult<T1>> pack
    ) {
        return new Codec<T1>() {
            @Override
            public <T> DataResult<Pair<T1, T>> decode(DynamicOps<T> ops, T input) {
                DataResult<Pair<T2, T>> baseResult = baseCodec.decode(ops, input);
                return baseResult.get().map(
                        p -> {
                            DataResult<T1> mapped = pack.apply(p.getFirst());
                            return mapped.map(t1 -> Pair.of(t1, p.getSecond()));
                        },
                        //TODO not always empty?
                        partial -> DataResult.error(partial.message())
                );
            }

            @Override
            public <T> DataResult<T> encode(T1 input, DynamicOps<T> ops, T prefix) {
                return baseCodec.encode(unpack.apply(input), ops, prefix);
            }
        };
    }
}
