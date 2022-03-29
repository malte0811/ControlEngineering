package malte0811.controlengineering.util.mycodec;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import malte0811.controlengineering.util.mycodec.tree.TreeElement;
import malte0811.controlengineering.util.mycodec.tree.TreeManager;
import malte0811.controlengineering.util.mycodec.tree.TreePrimitive;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyCodecs {
    public static final MyCodec<Integer> INTEGER = new SimpleCodec<>(
            TreePrimitive.class, TreePrimitive::asInt, SerialStorage::writeInt, SerialStorage::readInt
    ) {
        @Override
        public <B> TreeElement<B> toTree(Integer in, TreeManager<B> manager) {
            return manager.makeInt(in);
        }
    };
    public static final MyCodec<Integer> HEX_INTEGER = new SimpleCodec<>(
            TreePrimitive.class, TreePrimitive::asInt, SerialStorage::writeHexInt, SerialStorage::readHexInt
    ) {
        @Override
        public <B> TreeElement<B> toTree(Integer in, TreeManager<B> manager) {
            return manager.makeInt(in);
        }
    };
    public static final MyCodec<Byte> BYTE = new SimpleCodec<>(
            TreePrimitive.class, TreePrimitive::asByte, SerialStorage::writeByte, SerialStorage::readByte
    ) {
        @Override
        public <B> TreeElement<B> toTree(Byte in, TreeManager<B> manager) {
            return manager.makeByte(in);
        }
    };
    public static final MyCodec<Float> FLOAT = new SimpleCodec<>(
            TreePrimitive.class, TreePrimitive::asFloat,
            SerialStorage::writeFloat, SerialStorage::readFloat
    ) {
        @Override
        public <B> TreeElement<B> toTree(Float in, TreeManager<B> manager) {
            return manager.makeFloat(in);
        }
    };
    public static final MyCodec<Double> DOUBLE = new SimpleCodec<>(
            TreePrimitive.class, TreePrimitive::asDouble,
            SerialStorage::writeDouble, SerialStorage::readDouble
    ) {
        @Override
        public <B> TreeElement<B> toTree(Double in, TreeManager<B> manager) {
            return manager.makeDouble(in);
        }
    };
    public static final MyCodec<ByteList> BYTE_LIST = list(BYTE).xmap(ByteArrayList::new, l -> l);
    public static final MyCodec<String> STRING = new SimpleCodec<>(
            TreePrimitive.class, TreePrimitive::asString,
            SerialStorage::writeString, SerialStorage::readString
    ) {
        @Override
        public <B> TreeElement<B> toTree(String in, TreeManager<B> manager) {
            return manager.makeString(in);
        }
    };
    public static final MyCodec<Boolean> BOOL = new SimpleCodec<>(
            TreePrimitive.class, TreePrimitive::asBool,
            SerialStorage::writeBoolean, SerialStorage::readBoolean
    ) {
        @Override
        public <B> TreeElement<B> toTree(Boolean in, TreeManager<B> manager) {
            return manager.makeBoolean(in);
        }
    };
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
                TreePrimitive.class, $ -> value, ($, $2) -> {}, $ -> FastDataResult.success(value)
        ) {
            @Override
            public <B> TreeElement<B> toTree(T in, TreeManager<B> manager) {
                return manager.makeByte((byte) 0);
            }
        };
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
