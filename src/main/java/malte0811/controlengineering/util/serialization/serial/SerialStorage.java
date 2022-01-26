package malte0811.controlengineering.util.serialization.serial;

import malte0811.controlengineering.util.FastDataResult;

public interface SerialStorage {
    void writeInt(int value, int base);

    default void writeInt(int value) {
        writeInt(value, 10);
    }

    default void writeHexInt(int value) {
        writeInt(value, 16);
    }

    FastDataResult<Integer> readInt(int base);

    default FastDataResult<Integer> readInt() {
        return readInt(10);
    }

    default FastDataResult<Integer> readHexInt() {
        return readInt(16);
    }

    void writeString(String value);

    FastDataResult<String> readString();

    void writeBoolean(boolean value);

    FastDataResult<Boolean> readBoolean();

    FastDataResult<Byte> readByte();

    void writeByte(byte value);

    FastDataResult<Float> readFloat();

    void writeFloat(float value);

    FastDataResult<Double> readDouble();

    void writeDouble(double value);
}
