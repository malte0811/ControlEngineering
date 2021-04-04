package malte0811.controlengineering.logic.schematic.symbol;

import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.util.Vec2i;

import java.util.Objects;

public class SymbolPin {
    private final Vec2i position;
    private final SignalType type;

    public SymbolPin(int x, int y, SignalType type) {
        this(new Vec2i(x, y), type);
    }

    public SymbolPin(Vec2i position, SignalType type) {
        this.position = position;
        this.type = type;
    }

    public static SymbolPin digital(int x, int y) {
        return new SymbolPin(x, y, SignalType.DIGITAL);
    }

    public static SymbolPin analog(int x, int y) {
        return new SymbolPin(x, y, SignalType.ANALOG);
    }

    public Vec2i getPosition() {
        return position;
    }

    public SignalType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolPin symbolPin = (SymbolPin) o;
        return position.equals(symbolPin.position) && type == symbolPin.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, type);
    }
}
