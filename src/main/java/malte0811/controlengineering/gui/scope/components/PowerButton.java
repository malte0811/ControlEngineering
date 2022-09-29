package malte0811.controlengineering.gui.scope.components;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.client.render.utils.ScreenUtils;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.network.chat.Component;

public class PowerButton implements IScopeComponent {
    public static final String ON_TOOLTIP_KEY = ControlEngineering.MODID + ".gui.scope.powerButtonOn";
    public static final String OFF_TOOLTIP_KEY = ControlEngineering.MODID + ".gui.scope.powerButtonOff";

    private final boolean active;
    private final RectangleI area;
    private final BooleanConsumer setActive;

    public PowerButton(boolean active, Vec2i pos, BooleanConsumer setActive) {
        this.active = active;
        this.area = new RectangleI(pos, pos.add(24, 7));
        this.setActive = setActive;
    }

    @Override
    public void render(PoseStack transform) {
        final int color = (active ? 0xff00 : 0x244324) | (0xff << 24);
        ScreenUtils.fill(transform, area.minX() + 19, area.minY() + 1, area.maxX(), area.maxY() - 1, color);
    }

    @Override
    public boolean click(double x, double y) {
        if (x <= area.minX() + 16) {
            setActive.accept(!this.active);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public RectangleI getArea() {
        return area;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable(active ? ON_TOOLTIP_KEY : OFF_TOOLTIP_KEY);
    }

    @Override
    public boolean requiresPower() {
        // Would be funny if it did...
        return false;
    }
}
