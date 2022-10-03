package malte0811.controlengineering.gui.scope.components;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.IntConsumer;

import static malte0811.controlengineering.gui.scope.module.ClientModule.VERT_OFFSET_TOOLTIP;

public class Range implements IScopeComponent {
    private static final int TEXT_COLOR = 0xffffa54a;
    private final Component tooltip;
    private final int min;
    private final int max;
    private final int step;
    private final int altStep;
    private final int value;
    private final IntConsumer setValue;
    private final RectangleI area;

    private Range(
            Component tooltip, Vec2i pos, int min, int max, int step, int altStep, int value, IntConsumer setValue
    ) {
        Preconditions.checkState(altStep > 0);
        this.tooltip = tooltip;
        this.min = min;
        this.max = max;
        this.step = step;
        this.altStep = altStep;
        this.value = value;
        this.setValue = setValue;
        this.area = new RectangleI(pos, pos.add(21, 5));
    }

    public static Range makeExponential(
            Component tooltip, Vec2i pos, int min, int max, int linStep, int value, IntConsumer setValue
    ) {
        return new Range(tooltip, pos, min, max, 0, linStep, value, setValue);
    }

    public static Range makeLinear(
            Component tooltip, Vec2i pos, int min, int max, int step, int altStep, int value, IntConsumer setValue
    ) {
        Preconditions.checkState(step > 0);
        return new Range(tooltip, pos, min, max, step, altStep, value, setValue);
    }

    public static IScopeComponent makeVerticalOffset(Vec2i pos, int state, IntConsumer set) {
        return makeLinear(
                Component.translatable(VERT_OFFSET_TOOLTIP), pos, 0, 100, 1, 10, state, set
        );
    }

    @Override
    public void render(PoseStack transform) {
        final var text = Integer.toString(value);
        final var fontRender = Minecraft.getInstance().font;
        transform.pushPose();
        transform.translate(area.center().x(), area.center().y(), 0);
        final var maxWidth = fontRender.width("255") + 1;
        transform.scale(9f / maxWidth, 9f / maxWidth, 1);
        final var width = fontRender.width(text);
        final var posX = -width / 2f;
        final var posY = -fontRender.lineHeight / 2f + 1;
        fontRender.draw(transform, text, posX, posY, TEXT_COLOR);
        transform.popPose();
    }

    @Override
    public boolean click(double x, double y) {
        if (x <= getArea().minX() + 5) {
            changeValue(false);
            return true;
        } else if (x >= getArea().maxX() - 5) {
            changeValue(true);
            return true;
        } else {
            return false;
        }
    }

    private void changeValue(boolean up) {
        final var step = Screen.hasShiftDown() ? altStep : this.step;
        final var newValue = Mth.clamp(getNewValue(up, step), min, max);
        setValue.accept(newValue);
    }

    private int getNewValue(boolean up, int step) {
        if (step > 0) {
            return this.value + (up ? step : -step);
        } else {
            final int containingPower = Mth.smallestEncompassingPowerOfTwo(this.value);
            if (!up) {
                return containingPower / 2;
            } else if (containingPower == this.value) {
                return 2 * containingPower;
            } else {
                return containingPower;
            }
        }
    }

    @Override
    public RectangleI getArea() {
        return area;
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(tooltip);
    }

    @Override
    public boolean requiresPower() {
        return true;
    }
}
