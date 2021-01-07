package malte0811.controlengineering.util.serialization;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class PacketIO {
    protected final PacketBuffer buffer;

    protected PacketIO(PacketBuffer buffer) {
        this.buffer = buffer;
    }

    public abstract <T> T handle(T input, Type<T> type);

    public static PacketIO reader(PacketBuffer buffer) {
        return new Reader(buffer);
    }

    public static PacketIO writer(PacketBuffer buffer) {
        return new Writer(buffer);
    }

    private static class Reader extends PacketIO {
        private Reader(PacketBuffer buffer) {
            super(buffer);
        }

        @Override
        public <T> T handle(T input, Type<T> type) {
            return type.read.apply(buffer);
        }
    }

    private static class Writer extends PacketIO {
        private Writer(PacketBuffer buffer) {
            super(buffer);
        }

        @Override
        public <T> T handle(T input, Type<T> type) {
            type.write.accept(buffer, input);
            return input;
        }
    }

    public static class Type<T> {
        public static final Type<byte[]> BYTE_ARRAY = new Type<>(
                PacketBuffer::writeByteArray, PacketBuffer::readByteArray
        );
        public static final Type<int[]> VARINT_ARRAY = new Type<>(
                PacketBuffer::writeVarIntArray, PacketBuffer::readVarIntArray
        );
        public static final Type<long[]> LONG_ARRAY = new Type<>(
                PacketBuffer::writeLongArray, buf -> buf.readLongArray(null)
        );
        public static final Type<BlockPos> BLOCK_POS = new Type<>(
                PacketBuffer::writeBlockPos, PacketBuffer::readBlockPos
        );
        public static final Type<Integer> VARINT = new Type<>(
                PacketBuffer::writeVarInt, PacketBuffer::readVarInt
        );
        public static final Type<Long> VARLONG = new Type<>(
                PacketBuffer::writeVarLong, PacketBuffer::readVarLong
        );
        public static final Type<Integer> INT = new Type<>(
                PacketBuffer::writeInt, PacketBuffer::readInt
        );
        public static final Type<Long> LONG = new Type<>(
                PacketBuffer::writeLong, PacketBuffer::readLong
        );
        public static final Type<java.util.UUID> UUID = new Type<>(
                PacketBuffer::writeUniqueId, PacketBuffer::readUniqueId
        );
        public static final Type<String> STRING = new Type<>(
                PacketBuffer::writeString, PacketBuffer::readString
        );
        public static final Type<ResourceLocation> RESOURCE_LOCATION = new Type<>(
                PacketBuffer::writeResourceLocation, PacketBuffer::readResourceLocation
        );
        public static final Type<CompoundNBT> COMPOUND_TAG = new Type<>(
                PacketBuffer::writeCompoundTag, PacketBuffer::readCompoundTag
        );

        private final BiConsumer<PacketBuffer, T> write;
        private final Function<PacketBuffer, T> read;

        private Type(BiConsumer<PacketBuffer, T> write, Function<PacketBuffer, T> read) {
            this.write = write;
            this.read = read;
        }

        public Type<List<T>> listOf() {
            return new Type<>((pb, list) -> {
                pb.writeVarInt(list.size());
                for (T ele : list) {
                    write.accept(pb, ele);
                }
            }, pb -> {
                final int length = pb.readVarInt();
                List<T> result = new ArrayList<>(length);
                for (int i = 0; i < length; ++i) {
                    result.add(read.apply(pb));
                }
                return result;
            });
        }
    }
}
