package malte0811.controlengineering.gui.scope.module;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.gui.scope.ScopeScreen;
import malte0811.controlengineering.gui.scope.components.IScopeComponent;
import malte0811.controlengineering.scope.module.ScopeModule;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;

import java.util.List;
import java.util.function.Consumer;

import static malte0811.controlengineering.gui.scope.ScopeScreen.*;

public abstract class ClientModule<T> {
    public static final String TRIGGER_SOURCE_USED = ControlEngineering.MODID + ".gui.scope.triggerEnabled";
    public static final String TRIGGER_SOURCE_UNUSED = ControlEngineering.MODID + ".gui.scope.triggerDisabled";
    public static final String CHANNEL_SHOWN = ControlEngineering.MODID + ".gui.scope.channelShown";
    public static final String CHANNEL_HIDDEN = ControlEngineering.MODID + ".gui.scope.channelHidden";
    public static final String MODULE_ACTIVE = ControlEngineering.MODID + ".gui.scope.moduleActive";
    public static final String MODULE_INACTIVE = ControlEngineering.MODID + ".gui.scope.moduleInactive";
    public static final String VERT_OFFSET_TOOLTIP = ControlEngineering.MODID + ".gui.scope.vertOffset";

    private final SubTexture texture;
    private final ScopeModule<T> serverModule;
    private final List<RectangleI> relativeChannelAreas;

    protected ClientModule(int slotOffsetInTexture, ScopeModule<T> serverModule) {
        final var minU = MODULE_U_OFFSET + slotOffsetInTexture * MODULE_SLOT_WIDTH;
        this.texture = new SubTexture(
                ScopeScreen.TEXTURE,
                minU, MODULE_V_MIN,
                minU + serverModule.getWidth() * MODULE_SLOT_WIDTH, MODULE_V_MAX
        );
        this.serverModule = serverModule;
        this.relativeChannelAreas = computeRelativeChannelAreas();
    }

    public abstract List<PoweredComponent> createComponents(
            Vec2i offset, T state, Consumer<T> setState, boolean scopePowered
    );

    protected abstract List<RectangleI> computeRelativeChannelAreas();

    public final SubTexture getTexture() {
        return texture;
    }

    public final ScopeModule<T> getServerModule() {
        return serverModule;
    }

    public final int getHoveredChannel(Vec2i moduleOffset, Vec2d mousePos) {
        final var relativeMousePos = mousePos.subtract(moduleOffset.x(), moduleOffset.y());
        for (int i = 0; i < relativeChannelAreas.size(); ++i) {
            if (relativeChannelAreas.get(i).containsClosed(relativeMousePos)) {
                return i;
            }
        }
        return -1;
    }

    public record PoweredComponent(IScopeComponent component, boolean canWork) {
        public PoweredComponent {
            canWork |= !component.requiresPower();
        }
    }
}
