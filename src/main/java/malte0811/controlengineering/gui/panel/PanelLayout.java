package malte0811.controlengineering.gui.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Quaternion;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.client.render.panel.PanelRenderer;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.network.panellayout.*;
import malte0811.controlengineering.util.ScreenUtils;
import malte0811.controlengineering.util.math.TransformUtil;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.List;

public class PanelLayout extends AbstractWidget {
    private static final double PANEL_GRID = 0.5;

    private final List<PlacedComponent> components;
    private PlacingComponent placing;

    public PanelLayout(int x, int y, int size, List<PlacedComponent> components) {
        super(x, y, size, size, TextComponent.EMPTY);
        this.components = components;
    }

    public void setPlacingComponent(PanelComponentType<?, ?> placing) {
        this.placing = new PlacingComponent(placing.newInstance(), Vec2d.ZERO);
    }

    @Override
    public void renderButton(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTicks) {
        TextureAtlasSprite texture = PanelRenderer.PANEL_TEXTURE.get();
        ScreenUtils.bindForShader(texture);
        transform.pushPose();
        transform.translate(x, y, 0);
        blit(transform, 0, 0, 0, width, height, texture);
        transform.scale((float) getPixelSize(), (float) getPixelSize(), 1);
        transform.translate(0, 0, 2);
        transform.mulPose(new Quaternion(-90, 0, 0, true));
        TransformUtil.shear(transform, .1f, .1f);
        transform.scale(1, -1, 1);
        MixedModel model = ComponentRenderers.renderAll(components, transform);
        if (placing != null) {
            final var placingPos = placing.getPlacingPos(this, mouseX, mouseY);
            transform.pushPose();
            transform.translate(placingPos.x(), 0, placingPos.y());
            ComponentRenderers.render(model, placing.component(), transform);
            transform.popPose();
        }
        MultiBufferSource.BufferSource impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        model.renderTo(impl, new PoseStack(), LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY);
        impl.endBatch();
        transform.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        final double mouseXPanel = getPanelPos(mouseX, x);
        final double mouseYPanel = getPanelPos(mouseY, y);
        int hovered = PlacedComponent.getIndexAt(Minecraft.getInstance().level, components, mouseXPanel, mouseYPanel);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (placing == null) {
                if (hovered >= 0) {
                    var pickedComponent = components.get(hovered);
                    var offset = pickedComponent.getPosMin().subtract(
                            mouseXPanel - PANEL_GRID / 2, mouseYPanel - PANEL_GRID / 2
                    );
                    placing = new PlacingComponent(pickedComponent.getComponent(), offset);
                    delete(mouseX, mouseY);
                    return true;
                }
                return false;
            } else {
                final var placingPos = placing.getPlacingPos(this, (int) mouseX, (int) mouseY);
                PlacedComponent newComponent = new PlacedComponent(placing.component(), placingPos);
                if (processAndSend(new Add(newComponent))) {
                    placing = null;
                    return true;
                }
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && hovered >= 0) {
            final PlacedComponent hoveredComp = components.get(hovered);
            configure(hoveredComp.getPosMin(), hoveredComp.getComponent());
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            return delete(ScreenUtils.getMousePosition());
        }
        return false;
    }

    private boolean delete(Vec2d mouse) {
        return delete(mouse.x(), mouse.y());
    }

    private boolean delete(double mouseX, double mouseY) {
        return processAndSend(new Delete(new Vec2d(getPanelPos(mouseX, x), getPanelPos(mouseY, y))));
    }

    private <T> void configure(Vec2d pos, PanelComponentInstance<T, ?> instance) {
        DataProviderScreen<T> screen = DataProviderScreen.makeFor(
                TextComponent.EMPTY,
                instance.getConfig(),
                instance.getType().getConfigCodec(),
                config -> processAndSend(
                        new Replace(new PlacedComponent(instance.getType().newInstanceFromCfg(config), pos))
                )
        );
        if (screen != null) {
            Minecraft.getInstance().setScreen(screen);
        }
    }

    private double getPanelPos(double mouse, int base) {
        return (mouse - base) / getPixelSize();
    }

    private double getGriddedPanelPos(double mouse, int base) {
        return Math.floor(getPanelPos(mouse, base) / PANEL_GRID) * PANEL_GRID;
    }

    private double getPixelSize() {
        return height / 16.;
    }

    private boolean processAndSend(PanelSubPacket packet) {
        if (packet.process(Minecraft.getInstance().level, components)) {
            ControlEngineering.NETWORK.sendToServer(new PanelPacket(packet));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateNarration(@Nonnull NarrationElementOutput pNarrationElementOutput) {}

    private record PlacingComponent(PanelComponentInstance<?, ?> component, Vec2d offsetFromMouse) {
        public Vec2d getPlacingPos(PanelLayout relative, double mouseX, double mouseY) {
            var pixel = relative.getPixelSize();
            return new Vec2d(
                    relative.getGriddedPanelPos(mouseX + offsetFromMouse.x() * pixel, relative.x),
                    relative.getGriddedPanelPos(mouseY + offsetFromMouse.y() * pixel, relative.y)
            );
        }
    }
}
