package malte0811.controlengineering.util.serialization.serial;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.util.FastDataResult;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

public class StringListStorage implements SerialStorage {
    private final Deque<String> data;

    public StringListStorage(List<String> data) {
        this.data = new ArrayDeque<>(data);
    }

    public StringListStorage() {
        this(ImmutableList.of());
    }

    @Nonnull
    private FastDataResult<String> poll() {
        if (!data.isEmpty()) {
            return FastDataResult.success(data.poll());
        } else {
            return FastDataResult.error("Not enough data");
        }
    }

    @Override
    public void writeInt(int value, int base) {
        data.add(Integer.toString(value, base));
    }

    @Override
    public FastDataResult<Integer> readInt(int base) {
        return read(s -> Integer.parseInt(s, base), "base-" + base + " integer");
    }

    @Override
    public void writeString(String value) {
        data.add(value);
    }

    @Override
    public FastDataResult<String> readString() {
        if (!data.isEmpty()) {
            return FastDataResult.success(data.poll());
        } else {
            return FastDataResult.error("No more data");
        }
    }

    @Override
    public void writeBoolean(boolean value) {
        data.push(Boolean.toString(value));
    }

    @Override
    public FastDataResult<Boolean> readBoolean() {
        return read(Boolean::parseBoolean, "boolean");
    }

    @Override
    public FastDataResult<Byte> readByte() {
        return read(Byte::parseByte, "byte");
    }

    @Override
    public void writeByte(byte value) {
        data.push(Byte.toString(value));
    }

    @Override
    public FastDataResult<Float> readFloat() {
        return read(Float::parseFloat, "float");
    }

    @Override
    public void writeFloat(float value) {
        data.push(Float.toString(value));
    }

    @Override
    public FastDataResult<Double> readDouble() {
        return read(Double::parseDouble, "double");
    }

    @Override
    public void writeDouble(double value) {
        data.push(Double.toString(value));
    }

    public List<String> getData() {
        return new ArrayList<>(data);
    }

    private <T> FastDataResult<T> read(Function<String, T> parse, String desc) {
        var maybeToken = poll();
        if (maybeToken.isError()) {
            return maybeToken.propagateError();
        }
        var token = maybeToken.get();
        try {
            return FastDataResult.success(parse.apply(token));
        } catch (Exception x) {
            return FastDataResult.error(token + " is not a valid " + desc);
        }
    }
}
