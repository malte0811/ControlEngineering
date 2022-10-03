package malte0811.controlengineering.gui.scope.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.client.render.utils.ScreenUtils;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.gui.scope.ScopeScreen;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BNCConnector implements IScopeComponent {
    private static final SubTexture BNC_PLUG = new SubTexture(ScopeScreen.TEXTURE, 227, 242, 241, 256);
    private static final int WIRE_COLOR = 0xff202020;

    private final RectangleI area;
    @Nullable
    private final BusSignalRef connectedTo;
    private final Component tooltip;
    private final Consumer<@Nullable BusSignalRef> setConnection;

    public BNCConnector(
            Vec2i pos,
            @Nullable BusSignalRef connectedTo,
            Component tooltip,
            Consumer<@Nullable BusSignalRef> setConnection
    ) {
        this.area = new RectangleI(pos, pos.add(14, 12));
        this.connectedTo = connectedTo;
        this.tooltip = tooltip;
        this.setConnection = setConnection;
    }

    @Override
    public void render(PoseStack transform) {
        if (connectedTo == null) { return; }
        BNC_PLUG.blit(transform, area.minX(), area.minY());
        transform.pushPose();
        final var center = area.center();
        transform.translate(center.x(), center.y(), 0);
        transform.mulPose(new Quaternion(0, 0, 10, true));
        // TODO make more flexible-looking?
        ScreenUtils.fill(transform, -3, 0, 3, 1000, WIRE_COLOR);
        ScreenUtils.fill(transform, -2, -0.5, 2, 1000, 0xff393939);
        ScreenUtils.fill(transform, -1, -1, 1, 1000, 0xff494949);
        transform.popPose();
    }

    @Override
    public boolean click(double x, double y) {
        if (connectedTo != null) {
            this.setConnection.accept(null);
        } else {
            Minecraft.getInstance().setScreen(DataProviderScreen.makeFor(
                    tooltip, BusSignalRef.DEFAULT, BusSignalRef.CODEC, setConnection
            ));
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
