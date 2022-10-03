package malte0811.controlengineering.gui.scope.components;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.client.render.utils.ScreenUtils;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PowerButton implements IScopeComponent {
    public static final String ON_TOOLTIP_KEY = ControlEngineering.MODID + ".gui.scope.powerButtonOn";
    public static final String OFF_TOOLTIP_KEY = ControlEngineering.MODID + ".gui.scope.powerButtonOff";
    public static final String BLACKOUT_TOOLTIP_KEY = ControlEngineering.MODID + ".gui.scope.noPower";
    public static final String POWER_TOOLTIP_KEY = ControlEngineering.MODID + ".gui.scope.powerUsage";

    private final boolean active;
    private final boolean canPower;
    private final int powerPerTick;
    private final RectangleI area;
    private final BooleanConsumer setActive;

    public PowerButton(boolean active, boolean canPower, int powerPerTick, Vec2i pos, BooleanConsumer setActive) {
        this.active = active;
        this.canPower = canPower;
        this.powerPerTick = powerPerTick;
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
        if (canPower && x <= area.minX() + 16) {
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
    public List<Component> getTooltip() {
        final Component firstLine;
        if (!canPower) {
            firstLine = Component.translatable(BLACKOUT_TOOLTIP_KEY).withStyle(ChatFormatting.RED);
        } else {
            firstLine = Component.translatable(active ? ON_TOOLTIP_KEY : OFF_TOOLTIP_KEY);
        }
        return List.of(firstLine, Component.translatable(POWER_TOOLTIP_KEY, powerPerTick));
    }

    @Override
    public boolean requiresPower() {
        // Would be funny if it did...
        return false;
    }
}
