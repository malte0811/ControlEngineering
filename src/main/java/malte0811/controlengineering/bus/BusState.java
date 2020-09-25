package malte0811.controlengineering.bus;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class BusState {
    private final List<BusLine> lines;

    public BusState(int width) {
        this.lines = new ArrayList<>(width);
        for (int i = 0; i < width; ++i) {
            this.lines.add(new BusLine());
        }
    }

    private BusState(List<BusLine> lines) {
        this.lines = lines;
    }

    public BusLine getLine(int index) {
        return lines.get(index);
    }

    public BusState withLine(int index, BusLine line) {
        List<BusLine> newLines = new ArrayList<>(lines);
        newLines.set(index, line);
        return new BusState(newLines);
    }

    public BusState with(int index, int color, int value) {
        BusLine newLine = getLine(index).with(color, value);
        return withLine(index, newLine);
    }

    public BusState merge(BusState other) {
        Preconditions.checkArgument(lines.size() == other.lines.size());
        List<BusLine> newLines = new ArrayList<>(lines.size());
        for (int line = 0; line < lines.size(); ++line) {
            newLines.add(getLine(line).merge(other.getLine(line)));
        }
        return new BusState(newLines);
    }
}
