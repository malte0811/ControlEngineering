package malte0811.controlengineering.gui.panel;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.client.render.PanelRenderer;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.List;

public class PanelLayout extends Widget {
    private final List<PlacedComponent> components;
    private PanelComponentInstance<?, ?> placing;

    public PanelLayout(int x, int y, int size, List<PlacedComponent> components) {
        super(x, y, size, size, StringTextComponent.EMPTY);
        this.components = components;
    }

    public void setPlacingComponent(PanelComponentType<?, ?> placing) {
        this.placing = placing.newInstance();
    }

    @Override
    public void renderWidget(@Nonnull MatrixStack transform, int mouseX, int mouseY, float partialTicks) {
        TextureAtlasSprite texture = PanelRenderer.PANEL_TEXTURE.get();
        texture.getAtlasTexture().bindTexture();
        transform.push();
        transform.translate(x, y, 0);
        blit(transform, 0, 0, 0, width, height, texture);
        transform.scale((float) getPixelSize(), (float) getPixelSize(), 1);
        GuiRenderTarget target = new GuiRenderTarget($ -> true);
        for (PlacedComponent comp : components) {
            renderComponent(comp, transform, target);
        }
        if (placing != null) {
            final double placingX = getGriddedPanelPos(mouseX, x);
            final double placingY = getGriddedPanelPos(mouseY, y);
            renderComponent(new PlacedComponent(placing, new Vec2d(placingX, placingY)), transform, target);
        }
        target.done();
        transform.pop();
    }

    private void renderComponent(PlacedComponent comp, MatrixStack transform, GuiRenderTarget target) {
        transform.push();
        transform.translate(comp.getPosMin().x, comp.getPosMin().y, 0);
        GuiRenderTarget.setupTopView(transform);
        ComponentRenderers.render(target, comp.getComponent(), transform);
        transform.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (placing == null) {
                return false;
            }
            final double placingX = getGriddedPanelPos(mouseX, x);
            final double placingY = getGriddedPanelPos(mouseY, y);
            //TODO disjointness and "within panel" checks
            components.add(new PlacedComponent(placing, new Vec2d(placingX, placingY)));
            placing = null;
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            final double panelX = getPanelPos(mouseX, x);
            final double panelY = getPanelPos(mouseY, y);
            for (PlacedComponent p : components) {
                if (p.getOutline().containsClosed(panelX, panelY)) {
                    configure(p.getComponent());
                    return true;
                }
            }
        }
        return false;
    }

    private <T> void configure(PanelComponentInstance<T, ?> instance) {
        DataProviderScreen<T> screen = DataProviderScreen.makeFor(
                StringTextComponent.EMPTY, instance.getConfig(), instance::setConfig
        );
        if (screen != null) {
            Minecraft.getInstance().displayGuiScreen(screen);
        }
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
}
