package malte0811.controlengineering.util.mycodec.serial;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import malte0811.controlengineering.util.FastDataResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StringListStorage implements SerialStorage {
    private final List<String> data;
    private final IntStack marks = new IntArrayList();
    private int nextReadIndex;

    public StringListStorage(List<String> data) {
        this.data = data;
    }

    public StringListStorage() {
        this(new ArrayList<>());
    }

    @Nonnull
    private FastDataResult<String> poll() {
        if (nextReadIndex < data.size()) {
            var result = FastDataResult.success(data.get(nextReadIndex));
            ++nextReadIndex;
            return result;
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
        return poll();
    }

    @Override
    public void writeBoolean(boolean value) {
        data.add(Boolean.toString(value));
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
        data.add(Byte.toString(value));
    }

    @Override
    public FastDataResult<Float> readFloat() {
        return read(Float::parseFloat, "float");
    }

    @Override
    public void writeFloat(float value) {
        data.add(Float.toString(value));
    }

    @Override
    public FastDataResult<Double> readDouble() {
        return read(Double::parseDouble, "double");
    }

    @Override
    public void writeDouble(double value) {
        data.add(Double.toString(value));
    }

    @Override
    public void pushMark() {
        marks.push(nextReadIndex);
    }

    @Override
    public void resetToMark() {
        nextReadIndex = marks.peekInt(0);
    }

    @Override
    public void popMark() {
        marks.popInt();
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
