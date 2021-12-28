package malte0811.controlengineering.bus;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.Arrays;

public class BusLine {
    public static final int LINE_SIZE = 16;
    public static final int MAX_VALID_VALUE = 255;
    public static final int MIN_VALID_VALUE = 0;
    public static final int RS_SCALE_FACTOR = 16;
    public static final BusLine EMPTY = new BusLine();

    private final int[] values;

    public BusLine() {
        this(new int[LINE_SIZE]);
    }

    private BusLine(Int2IntFunction rsValues) {
        int[] values = new int[LINE_SIZE];
        for (int i = 0; i < LINE_SIZE; ++i) {
            values[i] = rsValues.applyAsInt(i) * RS_SCALE_FACTOR;
        }
        this.values = values;
    }

    public BusLine(int[] values) {
        Preconditions.checkArgument(values.length == LINE_SIZE);
        for (int val : values) {
            Preconditions.checkArgument(val >= MIN_VALID_VALUE && val <= MAX_VALID_VALUE);
        }
        this.values = values;
    }

    public static BusLine fromRSState(byte[] in) {
        Preconditions.checkArgument(in.length == LINE_SIZE);
        return new BusLine(i -> in[i]);
    }

    public int getValue(int color) {
        return values[color];
    }

    public byte getRSValue(int color) {
        return (byte) (getValue(color) / RS_SCALE_FACTOR);
    }

    public BusLine with(int color, int value) {
        Preconditions.checkArgument(value >= MIN_VALID_VALUE && value <= MAX_VALID_VALUE);
        int[] newValues = Arrays.copyOf(values, LINE_SIZE);
        newValues[color] = value;
        return new BusLine(newValues);
    }

    public BusLine merge(BusLine other) {
        int[] newValues = new int[LINE_SIZE];
        for (int color = 0; color < LINE_SIZE; ++color) {
            newValues[color] = Math.max(getValue(color), other.getValue(color));
        }
        return new BusLine(newValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusLine busLine = (BusLine) o;
        return Arrays.equals(values, busLine.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }
}
