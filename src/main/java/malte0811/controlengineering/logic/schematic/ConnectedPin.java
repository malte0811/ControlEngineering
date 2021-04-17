package malte0811.controlengineering.logic.schematic;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolPin;
import malte0811.controlengineering.util.math.Vec2i;

import java.util.Objects;
import java.util.Set;

public class ConnectedPin {
    private final PlacedSymbol symbol;
    private final SymbolPin pin;

    public ConnectedPin(PlacedSymbol symbol, SymbolPin pin) {
        this.symbol = symbol;
        this.pin = pin;
    }

    public static boolean isConsistent(Set<ConnectedPin> netPins) {
        ConnectedPin sourcePin = null;
        boolean hasAnalogSource = false;
        boolean hasDigitalSink = false;
        int leftmostX = Integer.MAX_VALUE;
        for (ConnectedPin pin : netPins) {
            if (pin.getPin().isOutput()) {
                if (sourcePin != null) {
                    // Only allow one signal source
                    return false;
                }
                sourcePin = pin;
                if (pin.isAnalog()) {
                    hasAnalogSource = true;
                }
            } else if (!pin.isAnalog()) {
                hasDigitalSink = true;
            }
            if (leftmostX > pin.getPosition().x) {
                leftmostX = pin.getPosition().x;
            }
        }
        if (sourcePin != null && sourcePin.getPosition().x > leftmostX) {
            // there are pins left of the source pin
            return false;
        }
        // Do not allow analog source with digital sink
        return !(hasAnalogSource && hasDigitalSink);
    }

    public boolean isAnalog() {
        return pin.getType() == SignalType.ANALOG;
    }

    public SymbolPin getPin() {
        return pin;
    }

    public PlacedSymbol getSymbol() {
        return symbol;
    }

    public Vec2i getPosition() {
        return symbol.getPosition().add(pin.getPosition());
    }

    public void render(MatrixStack stack, int wireColor) {
        pin.render(stack, symbol.getPosition().x, symbol.getPosition().y, wireColor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectedPin that = (ConnectedPin) o;
        return symbol.equals(that.symbol) && pin.equals(that.pin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, pin);
    }
}
