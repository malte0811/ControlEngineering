package malte0811.controlengineering.util.serialization;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.Direction;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Codecs {
    public static final Codec<INBT> INBT_CODEC = Codec.PASSTHROUGH.flatXmap(
            dyn -> DataResult.success(dyn.convert(NBTDynamicOps.INSTANCE).getValue()),
            inbt -> DataResult.success(new Dynamic<>(NBTDynamicOps.INSTANCE, inbt))
    );

    public static final Codec<Direction> DIRECTION_CODEC = Codec.INT.xmap(
            i -> DirectionUtils.VALUES[i],
            Direction::ordinal
    );

    public static <K, V> Codec<Map<K, V>> codecForMap(Codec<K> key, Codec<V> value) {
        return Codec.list(safePair(key, value))
                .xmap(
                        l -> l.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)),
                        m -> m.entrySet().stream()
                                .map(e -> Pair.of(e.getKey(), e.getValue()))
                                .collect(Collectors.toList())
                );
    }

    public static <K, V> Codec<Pair<K, V>> safePair(Codec<K> left, Codec<V> right) {
        return RecordCodecBuilder.create(
                inst -> inst.group(
                        left.fieldOf("first").forGetter(Pair::getFirst),
                        right.fieldOf("second").forGetter(Pair::getSecond)
                ).apply(inst, Pair::of)
        );
    }

    public static <T> DataResult<T> read(Codec<T> codec, CompoundNBT in, String subName) {
        return read(codec, in.get(subName));
    }

    public static <T> DataResult<T> read(Codec<T> codec, INBT nbt) {
        return codec.decode(NBTDynamicOps.INSTANCE, nbt)
                .map(Pair::getFirst);
    }

    public static <T> void add(Codec<T> codec, T value, CompoundNBT out, String subName) {
        out.put(subName, encode(codec, value));
    }

    public static <T> INBT encode(Codec<T> codec, T value) {
        return codec.encodeStart(NBTDynamicOps.INSTANCE, value).getOrThrow(false, s -> {});
    }

    public static <T> T readOrNull(Codec<T> codec, INBT pinNBT) {
        return read(codec, pinNBT).result().orElse(null);
    }

    public static <T> T readOrThrow(Codec<T> codec, INBT data) {
        T result = readOrNull(codec, data);
        if (result == null) {
            throw new RuntimeException("Failed to read from " + data);
        } else {
            return result;
        }
    }

    public static <T> Optional<T> readOptional(Codec<T> codec, INBT data) {
        return read(codec, data).result();
    }
}
