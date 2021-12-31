package malte0811.controlengineering.util.serialization.serial;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.util.FastDataResult;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class StringListStorage implements SerialStorage {
    private final Deque<String> data;

    public StringListStorage(List<String> data) {
        this.data = new ArrayDeque<>(data);
    }

    public StringListStorage() {
        this(ImmutableList.of());
    }

    @Nonnull
    private String poll() {
        if (!data.isEmpty()) {
            return data.poll();
        } else {
            throw new RuntimeException("No more data");
        }
    }

    @Override
    public void writeInt(int value, int base) {
        data.add(Integer.toString(value, base));
    }

    @Override
    public FastDataResult<Integer> readInt(int base) {
        String token = poll();
        try {
            return FastDataResult.success(Integer.parseInt(token, base));
        } catch (Exception x) {
            return FastDataResult.error(token + " is not a base-" + base + " integer");
        }
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
        String token = poll();
        try {
            return FastDataResult.success(Boolean.parseBoolean(token));
        } catch (Exception x) {
            return FastDataResult.error(token + " is not a boolean");
        }
    }

    public List<String> getData() {
        return new ArrayList<>(data);
    }
}
