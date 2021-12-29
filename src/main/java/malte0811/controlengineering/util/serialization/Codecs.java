package malte0811.controlengineering.util.serialization;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Codecs {
    public static final Codec<Tag> INBT_CODEC = Codec.PASSTHROUGH.flatXmap(
            dyn -> DataResult.success(dyn.convert(NbtOps.INSTANCE).getValue()),
            inbt -> DataResult.success(new Dynamic<>(NbtOps.INSTANCE, inbt))
    );

    public static final Codec<Direction> DIRECTION_CODEC = Codec.INT.xmap(
            i -> DirectionUtils.VALUES[i],
            Direction::ordinal
    );

    public static final Codec<ByteList> BYTE_LIST_CODEC = Codec.BYTE.listOf()
            .xmap(ByteArrayList::new, Function.identity());

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

    public static <T> DataResult<T> read(Codec<T> codec, CompoundTag in, String subName) {
        return read(codec, in.get(subName));
    }

    public static <T> DataResult<T> read(Codec<T> codec, Tag nbt) {
        return codec.decode(NbtOps.INSTANCE, nbt)
                .map(Pair::getFirst);
    }

    public static <T> void add(Codec<T> codec, T value, CompoundTag out, String subName) {
        out.put(subName, encode(codec, value));
    }

    public static <T> Tag encode(Codec<T> codec, T value) {
        return codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, s -> {});
    }

    public static <T> T readOrNull(Codec<T> codec, Tag pinNBT) {
        return read(codec, pinNBT).result().orElse(null);
    }

    public static <T> T readOrThrow(Codec<T> codec, Tag data) {
        T result = readOrNull(codec, data);
        if (result == null) {
            throw new RuntimeException("Failed to read from " + data);
        } else {
            return result;
        }
    }

    public static <T> Optional<T> readOptional(Codec<T> codec, Tag data) {
        return read(codec, data).result();
    }
}
