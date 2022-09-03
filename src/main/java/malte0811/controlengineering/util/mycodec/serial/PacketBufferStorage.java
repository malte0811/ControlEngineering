package malte0811.controlengineering.util.mycodec.serial;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import malte0811.controlengineering.util.FastDataResult;
import net.minecraft.network.FriendlyByteBuf;

public class PacketBufferStorage implements SerialStorage {
    private final FriendlyByteBuf buffer;
    private final IntStack marks = new IntArrayList();

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

    @Override
    public FastDataResult<Byte> readByte() {
        return FastDataResult.success(buffer.readByte());
    }

    @Override
    public void writeByte(byte value) {
        buffer.writeByte(value);
    }

    @Override
    public FastDataResult<Float> readFloat() {
        return FastDataResult.success(buffer.readFloat());
    }

    @Override
    public void writeFloat(float value) {
        buffer.writeFloat(value);
    }

    @Override
    public FastDataResult<Double> readDouble() {
        return FastDataResult.success(buffer.readDouble());
    }

    @Override
    public void writeDouble(double value) {
        buffer.writeDouble(value);
    }

    @Override
    public FastDataResult<Long> readLong() {
        return FastDataResult.success(buffer.readLong());
    }

    @Override
    public void writeLong(long value) {
        buffer.writeLong(value);
    }


    @Override
    public FastDataResult<Short> readShort() {
        return FastDataResult.success(buffer.readShort());
    }

    @Override
    public void writeShort(short value) {
        buffer.writeShort(value);
    }

    @Override
    public void pushMark() {
        marks.push(buffer.readerIndex());
    }

    @Override
    public void resetToMark() {
        buffer.readerIndex(marks.peekInt(0));
    }

    @Override
    public void popMark() {
        marks.popInt();
    }
}
