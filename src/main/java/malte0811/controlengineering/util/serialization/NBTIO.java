package malte0811.controlengineering.util.serialization;

import net.minecraft.nbt.CompoundNBT;

public abstract class NBTIO {
    public static NBTIO reader(CompoundNBT nbt) {
        return new Reader(nbt);
    }

    public static NBTIO writer(CompoundNBT nbt) {
        return new Writer(nbt);
    }

    public abstract <T> T readOrWrite(String key, T value, ElementWriter<T> writer, ElementReader<T> reader);

    public final int handle(String key, int value) {
        return readOrWrite(key, value, CompoundNBT::putInt, CompoundNBT::getInt);
    }

    public final boolean handle(String key, boolean value) {
        return readOrWrite(key, value, CompoundNBT::putBoolean, CompoundNBT::getBoolean);
    }

    public final byte[] handle(String key, byte[] value) {
        return readOrWrite(key, value, CompoundNBT::putByteArray, CompoundNBT::getByteArray);
    }

    private static class Writer extends NBTIO {
        private final CompoundNBT output;

        private Writer(CompoundNBT output) {
            this.output = output;
        }

        @Override
        public <T> T readOrWrite(String key, T value, ElementWriter<T> writer, ElementReader<T> reader) {
            writer.write(output, key, value);
            return value;
        }
    }

    private static class Reader extends NBTIO {
        private final CompoundNBT input;

        private Reader(CompoundNBT output) {
            this.input = output;
        }

        @Override
        public <T> T readOrWrite(String key, T value, ElementWriter<T> writer, ElementReader<T> reader) {
            return reader.read(input, key);
        }
    }

    public interface ElementWriter<T> {
        void write(CompoundNBT nbt, String name, T value);
    }

    public interface ElementReader<T> {
        T read(CompoundNBT nbt, String name);
    }
}
