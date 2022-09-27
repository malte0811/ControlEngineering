package malte0811.controlengineering.gui.scope;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.client.CEShaders;
import malte0811.controlengineering.client.render.utils.ScreenUtils;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.gui.scope.components.IScopeComponent;
import malte0811.controlengineering.gui.scope.module.ClientModules;
import malte0811.controlengineering.network.scope.ModuleConfig;
import malte0811.controlengineering.network.scope.ScopePacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket.IScopeSubPacket;
import malte0811.controlengineering.scope.module.ScopeModuleInstance;
import malte0811.controlengineering.scope.trace.Trace;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ScopeScreen extends StackedScreen implements MenuAccess<ScopeMenu> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            ControlEngineering.MODID, "textures/gui/scope.png"
    );
    private static final SubTexture MAIN_TEXTURE = new SubTexture(TEXTURE, 0, 0, 218, 225);
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
        ScreenUtils.setupScissorMCScaled(leftPos + 5, topPos + 16, 159, 99);
        transform.pushPose();
        transform.translate(leftPos + 5, topPos + 16, 0);
        transform.scale(20f, 10f, 1);
        for (final var trace : menu.getTraces()) {
            drawTrace(transform, trace);
        }
        RenderSystem.disableScissor();
        transform.popPose();
        if (tooltip != null) {
            renderTooltip(transform, tooltip, mouseX, mouseY);
        }
    }

    private static final int[][] RG_SEQUENCE = {
            new int[]{0, 1}, new int[]{1, 1}, new int[]{1, 0}, new int[]{0, 0},
    };

    private static void putTraceVertex(
            VertexConsumer out, Matrix4f mat, double x, double y, int rgIndex, float scaledDelay, float alpha
    ) {
        final var rg = RG_SEQUENCE[rgIndex % RG_SEQUENCE.length];
        out.vertex(mat, (float) x, (float) y, 0.0F).color(rg[0], rg[1], scaledDelay, alpha).endVertex();
    }

    private static void fillTrace(
            PoseStack transform,
            double minX, double minY, double maxX, double maxY,
            float alpha, double delayLeft, double delayRight, boolean vertical
    ) {
        Matrix4f matrix = transform.last().pose();
        final var bufferbuilder = Tesselator.getInstance().getBuilder();
        final float scaledDLeft = Math.min(1, (float) (delayLeft / 1e4));
        final float scaledDRight = Math.min(1, (float) (delayRight / 1e4));
        final int indexOff = vertical ? 1 : 0;
        putTraceVertex(bufferbuilder, matrix, minX, maxY, indexOff, scaledDLeft, alpha);
        putTraceVertex(bufferbuilder, matrix, maxX, maxY, indexOff + 1, scaledDRight, alpha);
        putTraceVertex(bufferbuilder, matrix, maxX, minY, indexOff + 2, scaledDRight, alpha);
        putTraceVertex(bufferbuilder, matrix, minX, minY, indexOff + 3, scaledDLeft, alpha);
    }

    private void drawTrace(PoseStack transform, Trace trace) {
        // TODO get from BE!
        final double samplesPerDiv = 20;
        ScreenUtils.startPositionColorDraw();
        RenderSystem.setShader(CEShaders::getScopeTrace);
        final var samples = trace.getSamples();
        final long now = System.currentTimeMillis();
        for (int i = 0; i < samples.size() - 1; ++i) {
            final var yHere = 10 - samples.getDouble(i);
            final var delayHere = (float) (now - trace.getSampleTimestamps().getLong(i));
            final var delayNext = (float) (now - trace.getSampleTimestamps().getLong(i + 1));
            final var traceRadius = 0.1;
            fillTrace(
                    transform,
                    i / samplesPerDiv, yHere - traceRadius, (i + 1) / samplesPerDiv, yHere + traceRadius,
                    1, delayHere, delayNext, false
            );
            final var yNext = 10 - samples.getDouble(i + 1);
            final var minY = Math.min(yNext, yHere);
            final var maxY = Math.max(yNext, yHere);
            final var height = maxY - minY;
            final var alpha = (int) (255 * Mth.clamp(0.75 - height / 5, 0, 1));
            fillTrace(
                    transform,
                    (i + 0.5) / samplesPerDiv, minY,
                    (i + 1.5) / samplesPerDiv, maxY,
                    alpha, delayHere, delayHere, true
            );
        }
        ScreenUtils.endPositionColorDraw();
    }

    // TODO cache result
    private List<IScopeComponent> getComponents() {
        List<IScopeComponent> components = new ArrayList<>();
        int offset = 0;
        for (int i = 0; i < menu.getModules().size(); ++i) {
            final var module = menu.getModules().get(i);
            components.addAll(gatherComponentsFor(module.module(), i, offset));
            offset += module.type().getWidth();
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
        if (ScopeSubPacket.processFull(data, menu.getModules(), menu.getTraces())) {
            ControlEngineering.NETWORK.sendToServer(new ScopePacket(data));
        }
    }
}
