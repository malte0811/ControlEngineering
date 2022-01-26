package malte0811.controlengineering.util.serialization.mycodec;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.serialization.mycodec.record.CodecField;
import malte0811.controlengineering.util.serialization.mycodec.record.RecordCodec2;
import malte0811.controlengineering.util.serialization.serial.SerialStorage;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyCodecs {
    public static final MyCodec<Integer> INTEGER = new SimpleCodec<>(
            IntTag.class, IntTag::valueOf, IntTag::getAsInt, SerialStorage::writeInt, SerialStorage::readInt
    );
    public static final MyCodec<Integer> HEX_INTEGER = new SimpleCodec<>(
            IntTag.class, IntTag::valueOf, IntTag::getAsInt, SerialStorage::writeHexInt, SerialStorage::readHexInt
    );
    public static final MyCodec<Byte> BYTE = new SimpleCodec<>(
            ByteTag.class, ByteTag::valueOf, ByteTag::getAsByte, SerialStorage::writeByte, SerialStorage::readByte
    );
    public static final MyCodec<Float> FLOAT = new SimpleCodec<>(
            FloatTag.class, FloatTag::valueOf, FloatTag::getAsFloat,
            SerialStorage::writeFloat, SerialStorage::readFloat
    );
    public static final MyCodec<Double> DOUBLE = new SimpleCodec<>(
            DoubleTag.class, DoubleTag::valueOf, DoubleTag::getAsDouble,
            SerialStorage::writeDouble, SerialStorage::readDouble
    );
    public static final MyCodec<ByteList> BYTE_LIST = list(BYTE).xmap(ByteArrayList::new, l -> l);
    public static final MyCodec<String> STRING = new SimpleCodec<>(
            StringTag.class, StringTag::valueOf, StringTag::getAsString,
            SerialStorage::writeString, SerialStorage::readString
    );
    public static final MyCodec<Boolean> BOOL = new SimpleCodec<>(
            ByteTag.class, ByteTag::valueOf, t -> (t.getAsByte() != 0),
            SerialStorage::writeBoolean, SerialStorage::readBoolean
    );
    //TODO handle exceptions?
    public static final MyCodec<ResourceLocation> RESOURCE_LOCATION = STRING.xmap(
            ResourceLocation::new,
            ResourceLocation::toString
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
        return new SimpleCodec<>(
                ByteTag.class, $ -> ByteTag.ZERO, $ -> value, ($, $2) -> {}, $ -> FastDataResult.success(value)
        );
    }

    public static <K, V> MyCodec<Map<K, V>> codecForMap(MyCodec<K> keyCodec, MyCodec<V> valueCodec) {
        return list(pair(keyCodec, valueCodec)).xmap(
                l -> l.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)),
                m -> m.entrySet().stream()
                        .map(e -> Pair.of(e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
        );
    }
}
