package malte0811.controlengineering.gui.scope.module;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.scope.ScopeModule;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.gui.scope.ScopeScreen;
import malte0811.controlengineering.gui.scope.components.IScopeComponent;
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

    private final SubTexture texture;
    private final ScopeModule<T> serverModule;

    protected ClientModule(int slotOffsetInTexture, ScopeModule<T> serverModule) {
        final var minU = MODULE_U_OFFSET + slotOffsetInTexture * MODULE_SLOT_WIDTH;
        this.texture = new SubTexture(
                ScopeScreen.TEXTURE,
                minU, MODULE_V_MIN,
                minU + serverModule.getWidth() * MODULE_SLOT_WIDTH, MODULE_V_MAX
        );
        this.serverModule = serverModule;
    }

    public abstract List<IScopeComponent> createComponents(Vec2i offset, T state, Consumer<T> setState);

    public SubTexture getTexture() {
        return texture;
    }

    public ScopeModule<T> getServerModule() {
        return serverModule;
    }
}
