package malte0811.controlengineering.gui.panel;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.controlpanels.renders.PanelRenderer;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.network.panellayout.*;
import malte0811.controlengineering.util.GuiUtil;
import malte0811.controlengineering.util.math.TransformUtil;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Quaternion;
import java.util.List;

public class PanelLayout extends AbstractWidget {
    private final List<PlacedComponent> components;
    private PanelComponentInstance<?, ?> placing;

    public PanelLayout(int x, int y, int size, List<PlacedComponent> components) {
        super(x, y, size, size, TextComponent.EMPTY);
        this.components = components;
    }

    public void setPlacingComponent(PanelComponentType<?, ?> placing) {
        this.placing = placing.newInstance();
    }

    @Override
    public void renderButton(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTicks) {
        TextureAtlasSprite texture = PanelRenderer.PANEL_TEXTURE.get();
        texture.atlas().bind();
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
            final double placingX = getGriddedPanelPos(mouseX, x);
            final double placingY = getGriddedPanelPos(mouseY, y);
            transform.pushPose();
            transform.translate(placingX, 0, placingY);
            ComponentRenderers.render(model, placing, transform);
            transform.popPose();
        }
        MultiBufferSource.BufferSource impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        model.renderTo(impl, new PoseStack(), LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY);
        impl.endBatch();
        transform.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (placing == null) {
                final int hovered = getHoveredIndex(mouseX, mouseY);
                if (hovered >= 0) {
                    placing = components.get(hovered).getComponent();
                    delete(mouseX, mouseY);
                    return true;
                }
                return false;
            } else {
                final double placingX = getGriddedPanelPos(mouseX, x);
                final double placingY = getGriddedPanelPos(mouseY, y);
                PlacedComponent newComponent = new PlacedComponent(placing, new Vec2d(placingX, placingY));
                if (processAndSend(new Add(newComponent))) {
                    placing = null;
                    return true;
                }
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            final int hovered = getHoveredIndex(mouseX, mouseY);
            if (hovered >= 0) {
                final PlacedComponent hoveredComp = components.get(hovered);
                configure(hoveredComp.getPosMin(), hoveredComp.getComponent());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            return delete(GuiUtil.getMousePosition());
        }
        return false;
    }

    private boolean delete(Vec2d mouse) {
        return delete(mouse.x, mouse.y);
    }

    private boolean delete(double mouseX, double mouseY) {
        return processAndSend(new Delete(new Vec2d(getPanelPos(mouseX, x), getPanelPos(mouseY, y))));
    }

    private <T> void configure(Vec2d pos, PanelComponentInstance<T, ?> instance) {
        DataProviderScreen<T> screen = DataProviderScreen.makeFor(
                TextComponent.EMPTY, instance.getConfig(), config -> {
                    processAndSend(new Replace(new PlacedComponent(instance.getType().newInstance(config), pos)));
                }
        );
        if (screen != null) {
            Minecraft.getInstance().setScreen(screen);
        }
    }

    private int getHoveredIndex(Vec2d mouse) {
        return getHoveredIndex(mouse.x, mouse.y);
    }

    private int getHoveredIndex(double mouseX, double mouseY) {
        final double panelX = getPanelPos(mouseX, x);
        final double panelY = getPanelPos(mouseY, y);
        return PlacedComponent.getIndexAt(components, panelX, panelY);
    }

    private double getPanelPos(double mouse, int base) {
        return (mouse - base) / getPixelSize();
    }

    private double getGriddedPanelPos(double mouse, int base) {
        return ((int) (getPanelPos(mouse, base) * 2)) / 2.;
    }

    private double getPixelSize() {
        return height / 16.;
    }

    private boolean processAndSend(PanelSubPacket packet) {
        if (packet.process(components)) {
            ControlEngineering.NETWORK.sendToServer(new PanelPacket(packet));
            return true;
        } else {
            return false;
        }
    }
}
