package malte0811.controlengineering.util.serialization.serial;

import com.mojang.serialization.DataResult;

public interface SerialStorage {
    void writeInt(int value, int base);

    default void writeInt(int value) {
        writeInt(value, 10);
    }

    default void writeHexInt(int value) {
        writeInt(value, 16);
    }

    DataResult<Integer> readInt(int base);

    default DataResult<Integer> readInt() {
        return readInt(10);
    }

    default DataResult<Integer> readHexInt() {
        return readInt(16);
    }

    void writeString(String value);

    DataResult<String> readString();

    void writeBoolean(boolean value);

    DataResult<Boolean> readBoolean();
}
