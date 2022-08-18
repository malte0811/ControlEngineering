package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.schematic.WireSegment;
import malte0811.controlengineering.client.render.utils.ScreenUtils;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.util.Mth;

public record SymbolPin(
        Vec2i position, SignalType type, PinDirection direction, String pinName, boolean vertical
) {
    public SymbolPin(Vec2i position, SignalType type, PinDirection direction, String pinName) {
        this(position, type, direction, pinName, false);
    }

    public SymbolPin(int x, int y, SignalType type, PinDirection direction, String pinName) {
        this(new Vec2i(x, y), type, direction, pinName);
    }

    public static SymbolPin digitalOut(int x, int y, String name) {
        return new SymbolPin(x, y, SignalType.DIGITAL, PinDirection.OUTPUT, name);
    }

    public static SymbolPin analogOut(int x, int y, String name) {
        return new SymbolPin(x, y, SignalType.ANALOG, PinDirection.OUTPUT, name);
    }

    public static SymbolPin analogIn(int x, int y, String name) {
        return new SymbolPin(x, y, SignalType.ANALOG, PinDirection.INPUT, name);
    }

    public static SymbolPin digitalIn(int x, int y, String name) {
        return new SymbolPin(x, y, SignalType.DIGITAL, PinDirection.INPUT, name);
    }

    public boolean isOutput() {
        return direction.isOutput();
    }

    public boolean isCombinatorialOutput() {
        return direction.isCombinatorialOutput();
    }

    public void render(PoseStack stack, int x, int y, int wireColor) {
        final Vec2i pinPos = position().add(x, y);
        if (vertical) {
            stack.pushPose();
            stack.translate(pinPos.x() + 0.5, pinPos.y() + 0.5, 0);
            stack.mulPose(new Quaternion(0, 0, -Mth.HALF_PI, false));
            stack.translate(-0.5 - pinPos.x(), -0.5 - pinPos.y(), 0);
        }
        final int wirePixels = 1;
        final float wireXMin = pinPos.x() + (isOutput() ? -wirePixels : WireSegment.WIRE_SPACE);
        final float wireXMax = pinPos.x() + (isOutput() ? 1 - WireSegment.WIRE_SPACE : (1 + wirePixels));
        final float yMin = pinPos.y() + WireSegment.WIRE_SPACE;
        final float yMax = pinPos.y() + 1 - WireSegment.WIRE_SPACE;
        ScreenUtils.fill(stack, wireXMin, yMin, wireXMax, yMax, wireColor);
        final int color = isOutput() ? 0xffff0000 : 0xff00ff00;
        ScreenUtils.fill(
                stack,
                pinPos.x() + WireSegment.WIRE_SPACE, yMin,
                pinPos.x() + 1 - WireSegment.WIRE_SPACE, yMax,
                color
        );
        if (vertical) {
            stack.popPose();
        }
    }
}
