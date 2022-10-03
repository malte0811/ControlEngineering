package malte0811.controlengineering.gui.scope.components;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusWireType;
import malte0811.controlengineering.client.render.utils.ScreenUtils;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.gui.misc.IDataProviderWidget;
import malte0811.controlengineering.gui.widget.BasicSlider;
import malte0811.controlengineering.scope.module.DigitalModule;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.TransformUtil;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.IntConsumer;

import static malte0811.controlengineering.gui.misc.BusSignalSelector.BUS_LINE_INDEX_KEY;
import static malte0811.controlengineering.scope.module.DigitalModule.NO_LINE;

public class LogicConnector implements IScopeComponent {
    private static final int WIRE_COLOR_DARK = 0xff202020;
    private static final int WIRE_COLOR_LIGHT = 0xff404040;

    private final RectangleI area;
    private final int connectedLine;
    private final Component tooltip;
    private final IntConsumer setConnection;

    public LogicConnector(
            Vec2i pos, int connectedLine, Component tooltip, IntConsumer setConnection
    ) {
        this.area = new RectangleI(pos, pos.add(19, 4));
        this.connectedLine = connectedLine;
        this.tooltip = tooltip;
        this.setConnection = setConnection;
    }

    @Override
    public void render(PoseStack transform) {
        if (connectedLine == DigitalModule.NO_LINE) { return; }
        ScreenUtils.fill(transform, area.minX(), area.minY(), area.maxX(), area.maxY(), 0xff << 24);
        transform.pushPose();
        transform.translate(area.minX() + 1, area.center().y(), 0);
        TransformUtil.shear(transform, -1 / 5f, 0);
        ScreenUtils.fill(transform, 0, 0, 17, 100, WIRE_COLOR_DARK);
        final int numLines = 8;
        final double lineWidth = 17 / (double) numLines;
        for (int i = 0; i < numLines; ++i) {
            final var startLine = lineWidth * i + lineWidth / 4;
            ScreenUtils.fill(transform, startLine, 0, startLine + lineWidth / 2, 100, WIRE_COLOR_LIGHT);
        }
        transform.popPose();
    }

    @Override
    public boolean click(double mouseX, double mouseY) {
        if (connectedLine != NO_LINE) {
            this.setConnection.accept(NO_LINE);
        } else {
            IDataProviderWidget.Factory<Integer, BasicSlider> factory = (currentValue, x, y) -> {
                final var initial = currentValue != null ? currentValue : 0;
                return new BasicSlider(x, y, 80, 20, 0, BusWireType.NUM_LINES - 1, BUS_LINE_INDEX_KEY, initial);
            };
            Minecraft.getInstance().setScreen(new DataProviderScreen<>(tooltip, factory, 0, setConnection::accept));
        }
        return true;
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
        return false;
    }
}
