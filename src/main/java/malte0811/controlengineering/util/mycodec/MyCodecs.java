package malte0811.controlengineering.util.mycodec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class MyCodecs {
    public static final MyCodec<Integer> INTEGER = new SimpleCodec<>(
            Codec.INT, SerialStorage::writeInt, SerialStorage::readInt
    );
    public static final MyCodec<Short> SHORT = new SimpleCodec<>(
            Codec.SHORT, SerialStorage::writeShort, SerialStorage::readShort
    );
    public static final MyCodec<Long> LONG = new SimpleCodec<>(
            Codec.LONG, SerialStorage::writeLong, SerialStorage::readLong
    );
    public static final MyCodec<Integer> HEX_COLOR = new SimpleCodec<>(
            Codec.INT, SerialStorage::writeHexInt, SerialStorage::readHexInt
    );
    public static final MyCodec<Byte> BYTE = new SimpleCodec<>(
            Codec.BYTE, SerialStorage::writeByte, SerialStorage::readByte
    );
    public static final MyCodec<Float> FLOAT = new SimpleCodec<>(
            Codec.FLOAT, SerialStorage::writeFloat, SerialStorage::readFloat
    );
    public static final MyCodec<Double> DOUBLE = new SimpleCodec<>(
            Codec.DOUBLE, SerialStorage::writeDouble, SerialStorage::readDouble
    );
    public static final MyCodec<ByteList> BYTE_LIST = list(BYTE).xmap(ByteArrayList::new, l -> l);
    public static final MyCodec<IntList> INT_LIST = list(INTEGER).xmap(IntArrayList::new, l -> l);
    public static final MyCodec<String> STRING = new SimpleCodec<>(
            Codec.STRING, SerialStorage::writeString, SerialStorage::readString
    );
    public static final MyCodec<Boolean> BOOL = new SimpleCodec<>(
            Codec.BOOL, SerialStorage::writeBoolean, SerialStorage::readBoolean
    );
    //TODO handle exceptions?
    public static final MyCodec<ResourceLocation> RESOURCE_LOCATION = STRING.xmap(
            ResourceLocation::parse, ResourceLocation::toString
    );
    public static final MyCodec<UUID> UUID_CODEC = new RecordCodec2<>(
            new CodecField<>("msb", UUID::getMostSignificantBits, LONG),
            new CodecField<>("lsb", UUID::getLeastSignificantBits, LONG),
            UUID::new
    );

    public static <T> MyCodec<List<T>> list(MyCodec<T> in) {
        return new ListCodec<>(in);
    }

    public static <T1, T2>
    MyCodec<Pair<T1, T2>> pair(MyCodec<T1> first, MyCodec<T2> second) {
        return new RecordCodec2<>(
                new CodecField<>("first", Pair::getFirst, first),
                new CodecField<>("second", Pair::getSecond, second),
                Pair::of
        );
    }

    public static <T> MyCodec<T> unit(T value) {
        return new SimpleCodec<>(Codec.unit(value), ($, $2) -> { }, $ -> FastDataResult.success(value));
    }

    public static <K, V> MyCodec<Map<K, V>> codecForMap(MyCodec<K> keyCodec, MyCodec<V> valueCodec) {
        return list(pair(keyCodec, valueCodec)).xmap(
                l -> l.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)),
                m -> m.entrySet().stream()
                        .map(e -> Pair.of(e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
        );
    }

    public static <T> MyCodec<Optional<T>> optional(MyCodec<T> fullCodec) {
        return BOOL.dispatch(
                Optional::isPresent,
                present -> {
                    if (present != Boolean.TRUE) {
                        return unit(Optional.empty());
                    } else {
                        return fullCodec.xmap(Optional::of, Optional::get);
                    }
                },
                "isNull", "value"
        );
    }

    public static <E extends Enum<E>>
    MyCodec<E> forEnum(E[] values, ToIntFunction<E> ordinal) {
        return INTEGER.xmap(i -> values[i], ordinal::applyAsInt);
    }
}
