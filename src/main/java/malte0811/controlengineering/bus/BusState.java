package malte0811.controlengineering.bus;

import java.util.ArrayList;
import java.util.List;

public class BusState {
    public static final BusState EMPTY = new BusState();

    private final List<BusLine> lines;

    private BusState() {
        this.lines = new ArrayList<>(BusWireType.NUM_LINES);
        for (int i = 0; i < BusWireType.NUM_LINES; ++i) {
            this.lines.add(new BusLine());
        }
    }

    private BusState(List<BusLine> lines) {
        this.lines = lines;
    }

    public BusLine getLine(int index) {
        if (index < lines.size()) {
            return lines.get(index);
        } else {
            return BusLine.EMPTY;
        }
    }

    public BusState withLine(int index, BusLine line) {
        List<BusLine> newLines = new ArrayList<>(lines);
        newLines.set(index, line);
        return new BusState(newLines);
    }

    public BusState with(int index, int color, int value) {
        return with(new BusSignalRef(index, color), value);
    }

    public BusState with(BusSignalRef signal, int value) {
        BusLine newLine = getLine(signal.line()).with(signal.color(), value);
        return withLine(signal.line(), newLine);
    }

    public int getWidth() {
        return lines.size();
    }

    public BusState merge(BusState other) {
        // Result has same width as this
        if (other.getWidth() > getWidth()) {
            return other.merge(this);
        }
        List<BusLine> newLines = new ArrayList<>(lines);
        for (int line = 0; line < other.getWidth(); ++line) {
            newLines.set(line, getLine(line).merge(other.getLine(line)));
        }
        return new BusState(newLines);
    }

    public int getSignal(BusSignalRef signal) {
        if (signal.line() < lines.size()) {
            BusLine line = getLine(signal.line());
            return line.getValue(signal.color());
        } else {
            return 0;
        }
    }
}
