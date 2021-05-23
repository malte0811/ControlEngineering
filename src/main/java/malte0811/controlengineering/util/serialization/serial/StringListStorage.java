package malte0811.controlengineering.util.serialization.serial;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.DataResult;

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
    public DataResult<Integer> readInt(int base) {
        try {
            return DataResult.success(Integer.parseInt(poll(), base));
        } catch (Exception x) {
            return DataResult.error(x.getMessage());
        }
    }

    @Override
    public void writeString(String value) {
        data.add(value);
    }

    @Override
    public DataResult<String> readString() {
        if (!data.isEmpty()) {
            return DataResult.success(data.poll());
        } else {
            return DataResult.error("No more data");
        }
    }

    @Override
    public void writeBoolean(boolean value) {
        data.push(Boolean.toString(value));
    }

    @Override
    public DataResult<Boolean> readBoolean() {
        try {
            return DataResult.success(Boolean.parseBoolean(poll()));
        } catch (Exception x) {
            return DataResult.error(x.getMessage());
        }
    }

    public List<String> getData() {
        return new ArrayList<>(data);
    }
}
