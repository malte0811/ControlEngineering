package malte0811.controlengineering.gui.scope;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.gui.scope.components.IScopeComponent;
import malte0811.controlengineering.gui.scope.components.PowerButton;
import malte0811.controlengineering.gui.scope.components.Range;
import malte0811.controlengineering.gui.scope.components.ScopeButton;
import malte0811.controlengineering.gui.scope.module.ClientModules;
import malte0811.controlengineering.network.scope.*;
import malte0811.controlengineering.network.scope.ScopeSubPacket.IScopeSubPacket;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.module.ScopeModuleInstance;
import malte0811.controlengineering.scope.trace.TraceId;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScopeScreen extends StackedScreen implements MenuAccess<ScopeMenu> {
    public static final String TICKS_PER_DIV_KEY = ControlEngineering.MODID + ".gui.scope.ticksPerDiv";
    public static final String ARM_TRIGGER_KEY = ControlEngineering.MODID + ".gui.scope.armTrigger";
    public static final String FORCE_TRIGGER_KEY = ControlEngineering.MODID + ".gui.scope.forceTrigger";
    public static final String RESET_KEY = ControlEngineering.MODID + ".gui.scope.reset";

    public static final ResourceLocation TEXTURE = new ResourceLocation(
            ControlEngineering.MODID, "textures/gui/scope.png"
    );
    private static final SubTexture MAIN_TEXTURE = new SubTexture(TEXTURE, 0, 0, 218, 225);
    public static final int MODULE_SLOT_WIDTH = 49;
    public static final int MODULE_V_MIN = 124;
    public static final int MODULE_V_MAX = 213;
    public static final int MODULE_U_OFFSET = 11;
    private static final RectangleI SCREEN_AREA = new RectangleI(5, 16, 164, 115);

    private final ScopeMenu menu;
    private int leftPos;
    private int topPos;
    private CRTDisplay crt;

    public ScopeScreen(ScopeMenu menu) {
        super(Component.empty());
        this.menu = menu;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - MAIN_TEXTURE.getWidth()) / 2;
        this.topPos = (this.height - MAIN_TEXTURE.getHeight()) / 2;
        this.crt = new CRTDisplay(menu, this.leftPos + 5, this.topPos + 16, 159, 99);
    }

    @Override
    protected void renderForeground(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTicks) {
        final var mousePos = new Vec2d(mouseX, mouseY);
        Component tooltip = null;
        final var scopePowered = menu.getGlobalConfig().powered();
        for (final var component : getComponents()) {
            if (scopePowered || !component.requiresPower()) {
                component.render(transform);
            }
            if (component.getArea().containsClosed(mousePos)) {
                tooltip = component.getTooltip();
            }
        }
        TraceId hovered = null;
        for (final var module : menu.getModules()) {
            final var clientModule = ClientModules.getModule(module.module().getType());
            final int hoveredTrace = clientModule.getHoveredChannel(getModuleOffset(module.firstSlot()), mousePos);
            if (hoveredTrace >= 0) {
                hovered = new TraceId(module.firstSlot(), hoveredTrace);
                break;
            }
        }
        crt.draw(transform, hovered);
        if (tooltip != null) {
            renderTooltip(transform, tooltip, mouseX, mouseY);
        }
    }

    // TODO cache result
    private List<IScopeComponent> getComponents() {
        List<IScopeComponent> components = makeTopLevelComponents();
        for (int i = 0; i < menu.getModules().size(); ++i) {
            final var module = menu.getModules().get(i);
            components.addAll(gatherComponentsFor(module.module(), i, module.firstSlot()));
        }
        return components;
    }

    private List<IScopeComponent> makeTopLevelComponents() {
        List<IScopeComponent> components = new ArrayList<>();
        final var globalCfg = menu.getGlobalConfig();
        final var origin = new Vec2i(this.leftPos, this.topPos);
        final Consumer<GlobalConfig> setCfg = cfg -> runAndSendToServer(new SetGlobalCfg(cfg));
        components.add(new PowerButton(
                globalCfg.powered(), origin.add(175, 32), b -> setCfg.accept(globalCfg.withPowered(b))
        ));
        if (!globalCfg.powered()) {
            return components;
        }
        components.add(Range.makeExponential(
                Component.translatable(TICKS_PER_DIV_KEY),
                origin.add(177, 49),
                2, 64, 1, globalCfg.ticksPerDiv(),
                i -> setCfg.accept(globalCfg.withTicksPerDiv(i))
        ));
        components.add(ScopeButton.makeOrange(
                origin.add(174, 73), globalCfg.triggerArmed(), Component.translatable(ARM_TRIGGER_KEY),
                () -> setCfg.accept(globalCfg.withTriggerArmed(true))
        ));
        components.add(ScopeButton.makeGreen(
                origin.add(174, 82), menu.getTraces().isSweeping(), Component.translatable(FORCE_TRIGGER_KEY),
                () -> {
                    if (!menu.getTraces().isSweeping()) {
                        runAndSendToServer(InitTraces.createForModules(menu.getModules(), globalCfg.ticksPerDiv()));
                    }
                }
        ));
        components.add(new ScopeButton(
                0xff5e29,
                Component.translatable(RESET_KEY),
                origin.add(174, 91),
                () -> runAndSendToServer(new ResetSweep())
        ));
        return components;
    }

    private <T> List<IScopeComponent> gatherComponentsFor(ScopeModuleInstance<T> module, int moduleIndex, int slot) {
        final var type = module.getType();
        return ClientModules.getModule(type).createComponents(
                getModuleOffset(slot),
                module.getCurrentState(),
                newState -> runAndSendToServer(new ModuleConfig(moduleIndex, type.newInstance(newState)))
        );
    }

    private Vec2i getModuleOffset(int slot) {
        return new Vec2i(this.leftPos + MODULE_U_OFFSET + slot * MODULE_SLOT_WIDTH, this.topPos + MODULE_V_MIN);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (SCREEN_AREA.containsClosed(mouseX - leftPos, mouseY - topPos)) {
            Minecraft.getInstance().setScreen(new ZoomedCRTScreen(menu));
            return true;
        }
        final var scopePowered = menu.getGlobalConfig().powered();
        for (final var component : getComponents()) {
            if (!component.getArea().containsClosed(mouseX, mouseY)) { continue; }
            if (!scopePowered && component.requiresPower()) { continue; }
            if (component.click(mouseX, mouseY)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderCustomBackground(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTicks) {
        super.renderCustomBackground(transform, mouseX, mouseY, partialTicks);
        transform.pushPose();
        transform.translate(this.leftPos, this.topPos, 0);
        MAIN_TEXTURE.blit(transform, 0, 0);
        transform.translate(MODULE_U_OFFSET, MODULE_V_MIN, 0);
        for (final var module : menu.getModules()) {
            final var texture = ClientModules.getModule(module.type()).getTexture();
            texture.blit(transform, 0, 0);
            transform.translate(MODULE_SLOT_WIDTH * module.type().getWidth(), 0, 0);
        }
        transform.popPose();
    }

    @Nonnull
    @Override
    public ScopeMenu getMenu() {
        return menu;
    }

    private void runAndSendToServer(IScopeSubPacket data) {
        if (ScopeSubPacket.processFull(data, menu)) {
            ControlEngineering.NETWORK.sendToServer(new ScopePacket(data));
        }
    }
}
