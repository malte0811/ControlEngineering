package malte0811.controlengineering.logic.schematic;

import com.mojang.blaze3d.vertex.PoseStack;
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
        return symbol.getPosition().add(pin.position());
    }

    public void render(PoseStack stack, int wireColor) {
        pin.render(stack, symbol.getPosition().x(), symbol.getPosition().y(), wireColor);
    }

    public RectangleI getShape() {
        final Vec2i basePos = getPosition();
        return new RectangleI(
                basePos.x() + (pin.isOutput() ? -1 : 0), basePos.y(),
                basePos.x() + (pin.isOutput() ? 1 : 2), basePos.y() + 1
        );
    }
}
