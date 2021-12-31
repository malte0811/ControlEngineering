package malte0811.controlengineering.util.serialization.serial;

import malte0811.controlengineering.util.FastDataResult;
import net.minecraft.network.FriendlyByteBuf;

public class PacketBufferStorage implements SerialStorage {
    private final FriendlyByteBuf buffer;

    public PacketBufferStorage(FriendlyByteBuf buffer) {
        this.buffer = buffer;
    }

    @Override
    public void writeInt(int value, int base) {
        buffer.writeVarInt(value);
    }

    @Override
    public FastDataResult<Integer> readInt(int base) {
        return FastDataResult.success(buffer.readVarInt());
    }

    @Override
    public void writeString(String value) {
        buffer.writeUtf(value);
    }

    @Override
    public FastDataResult<String> readString() {
        return FastDataResult.success(buffer.readUtf());
    }

    @Override
    public void writeBoolean(boolean value) {
        buffer.writeBoolean(value);
    }

    @Override
    public FastDataResult<Boolean> readBoolean() {
        return FastDataResult.success(buffer.readBoolean());
    }
}
