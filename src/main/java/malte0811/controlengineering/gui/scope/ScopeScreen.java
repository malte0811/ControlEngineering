package malte0811.controlengineering.gui.scope;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.gui.scope.components.IScopeComponent;
import malte0811.controlengineering.gui.scope.module.ClientModules;
import malte0811.controlengineering.network.scope.ModuleConfig;
import malte0811.controlengineering.network.scope.ScopePacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket.IScopeSubPacket;
import malte0811.controlengineering.scope.ScopeModuleInstance;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ScopeScreen extends StackedScreen implements MenuAccess<ScopeMenu> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            ControlEngineering.MODID, "textures/gui/scope.png"
    );
    private static final SubTexture MAIN_TEXTURE = new SubTexture(TEXTURE, 0, 0, 218, 225);
    private static final SubTexture BNC_COVER = new SubTexture(TEXTURE, 227, 242, 241, 256);
    private static final SubTexture SIGNAL_ENABLED = new SubTexture(TEXTURE, 241, 242, 244, 245);
    private static final SubTexture TRIGGER_ENABLED = new SubTexture(TEXTURE, 244, 242, 247, 245);
    public static final int MODULE_SLOT_WIDTH = 49;
    public static final int MODULE_V_MIN = 124;
    public static final int MODULE_V_MAX = 213;
    public static final int MODULE_U_OFFSET = 11;

    private final ScopeMenu menu;
    private int leftPos;
    private int topPos;

    public ScopeScreen(ScopeMenu menu) {
        super(Component.empty());
        this.menu = menu;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - MAIN_TEXTURE.getWidth()) / 2;
        this.topPos = (this.height - MAIN_TEXTURE.getHeight()) / 2;
    }

    @Override
    protected void renderForeground(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTicks) {
        Component tooltip = null;
        for (final var component : getComponents()) {
            component.render(transform);
            if (component.getArea().containsClosed(mouseX, mouseY)) {
                tooltip = component.getTooltip();
            }
        }
        if (tooltip != null) {
            renderTooltip(transform, tooltip, mouseX, mouseY);
        }
    }

    // TODO cache result
    private List<IScopeComponent> getComponents() {
        List<IScopeComponent> components = new ArrayList<>();
        int offset = 0;
        for (int i = 0; i < menu.getModules().size(); ++i) {
            final var module = menu.getModules().get(i);
            components.addAll(gatherComponentsFor(module, i, offset));
            offset += module.getType().getWidth();
        }
        return components;
    }

    private <T> List<IScopeComponent> gatherComponentsFor(
            ScopeModuleInstance<T> module, int moduleIndex, int offsetSlots
    ) {
        final var type = module.getType();
        return ClientModules.getModule(type).createComponents(
                new Vec2i(this.leftPos + MODULE_U_OFFSET + offsetSlots * MODULE_SLOT_WIDTH, this.topPos + MODULE_V_MIN),
                module.getCurrentState(),
                newState -> runAndSendToServer(new ModuleConfig(moduleIndex, type.newInstance(newState)))
        );
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (final var toggle : getComponents()) {
            if (toggle.getArea().containsClosed(pMouseX, pMouseY) && toggle.click(pMouseX, pMouseY)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void renderCustomBackground(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTicks) {
        super.renderCustomBackground(transform, mouseX, mouseY, partialTicks);
        transform.pushPose();
        transform.translate(this.leftPos, this.topPos, 0);
        MAIN_TEXTURE.blit(transform, 0, 0);
        transform.translate(MODULE_U_OFFSET, MODULE_V_MIN, 0);
        for (final var module : menu.getModules()) {
            final var texture = ClientModules.getModule(module.getType()).getTexture();
            texture.blit(transform, 0, 0);
            transform.translate(MODULE_SLOT_WIDTH * module.getType().getWidth(), 0, 0);
        }
        transform.popPose();
    }

    @Nonnull
    @Override
    public ScopeMenu getMenu() {
        return menu;
    }

    private void runAndSendToServer(IScopeSubPacket data) {
        if (ScopeSubPacket.processFull(data, menu.getModules())) {
            ControlEngineering.NETWORK.sendToServer(new ScopePacket(data));
        }
    }
}
