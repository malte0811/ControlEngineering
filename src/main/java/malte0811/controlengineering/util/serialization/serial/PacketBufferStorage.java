package malte0811.controlengineering.util.serialization.serial;

import com.mojang.serialization.DataResult;
import net.minecraft.network.PacketBuffer;

public class PacketBufferStorage implements SerialStorage {
    private final PacketBuffer buffer;

    public PacketBufferStorage(PacketBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void writeInt(int value, int base) {
        buffer.writeVarInt(value);
    }

    @Override
    public DataResult<Integer> readInt(int base) {
        return DataResult.success(buffer.readVarInt());
    }

    @Override
    public void writeString(String value) {
        buffer.writeString(value);
    }

    @Override
    public DataResult<String> readString() {
        return DataResult.success(buffer.readString());
    }

    @Override
    public void writeBoolean(boolean value) {
        buffer.writeBoolean(value);
    }

    @Override
    public DataResult<Boolean> readBoolean() {
        return DataResult.success(buffer.readBoolean());
    }
}
