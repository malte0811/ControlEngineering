package malte0811.controlengineering.gui.scope.components;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static malte0811.controlengineering.gui.scope.module.ClientModule.*;

public class ScopeButton implements IScopeComponent {
    private final int color;
    private final Component tooltip;
    private final Runnable click;
    private final RectangleI area;

    private static ScopeButton makeEnable(
            Vec2i pos, boolean enabled, String onTooltip, String offTooltip, Runnable toggle
    ) {
        final var tooltip = Component.translatable(enabled ? onTooltip : offTooltip);
        return new ScopeButton(enabled ? 0x26bd18 : 0x657163, tooltip, pos, toggle);
    }

    public static ScopeButton makeChannelEnable(Vec2i pos, boolean currentlyShown, Runnable toggle) {
        return makeEnable(pos, currentlyShown, CHANNEL_SHOWN, CHANNEL_HIDDEN, toggle);
    }

    public static ScopeButton makeModuleEnable(Vec2i pos, boolean currentlyShown, Runnable toggle) {
        return makeEnable(pos, currentlyShown, MODULE_ACTIVE, MODULE_INACTIVE, toggle);
    }

    public static ScopeButton makeTriggerEnable(Vec2i pos, boolean enabled, Runnable toggle) {
        final var tooltip = Component.translatable(enabled ? TRIGGER_SOURCE_USED : TRIGGER_SOURCE_UNUSED);
        return new ScopeButton(enabled ? 0xf2be22 : 0x796c45, tooltip, pos, toggle);
    }

    public ScopeButton(int color, Component tooltip, Vec2i pos, Runnable click) {
        this.color = color;
        this.tooltip = tooltip;
        this.click = click;
        this.area = new RectangleI(pos, pos.add(3, 3));
    }

    @Override
    public void render(PoseStack transform) {
        Screen.fill(transform, area.minX(), area.minY(), area.maxX(), area.maxY(), 0xff000000 | color);
    }

    @Override
    public boolean click(double x, double y) {
        if (!area.containsClosed(x, y)) { return false; }
        click.run();
        return true;
    }

    @Override
    public RectangleI getArea() {
        return area;
    }

    @Override
    public Component getTooltip() {
        return tooltip;
    }
}
