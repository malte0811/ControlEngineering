package malte0811.controlengineering.logic.schematic;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolPin;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2i;

public record ConnectedPin(PlacedSymbol symbol, SymbolPin pin) {

    public boolean isAnalog() {
        return pin.type() == SignalType.ANALOG;
    }

    public Vec2i getPosition() {
        return symbol.position().add(pin.position());
    }

    public void render(PoseStack stack, int wireColor) {
        pin.render(stack, symbol.position().x(), symbol.position().y(), wireColor, 0xff);
    }

    public RectangleI getShape() {
        return getBaseShape(pin.direction(), pin.vertical()).offset(getPosition());
    }

    private static RectangleI getBaseShape(PinDirection direction, boolean vertical) {
        if (vertical) {
            return new RectangleI(
                    0, (direction.isOutput() ? 0 : -1),
                    1, (direction.isOutput() ? 2 : 1)
            );
        } else {
            return new RectangleI(
                    (direction.isOutput() ? -1 : 0), 0,
                    (direction.isOutput() ? 1 : 2), 1
            );
        }
    }
}
