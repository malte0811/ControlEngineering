package malte0811.controlengineering.gui.panel;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.renders.PanelRenderer;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.network.panellayout.*;
import malte0811.controlengineering.util.GuiUtil;
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
        /*
        TODO
        GuiRenderTarget target = new GuiRenderTarget($ -> true);
        transform.translate(0, 0, 2);
        transform.rotate(new Quaternion(-90, 0, 0, true));
        TransformUtil.shear(transform, .1f, .1f);
        transform.scale(1, -1, 1);
        for (PlacedComponent comp : components) {
            renderComponent(comp, transform, target);
        }
        if (placing != null) {
            final double placingX = getGriddedPanelPos(mouseX, x);
            final double placingY = getGriddedPanelPos(mouseY, y);
            renderComponent(new PlacedComponent(placing, new Vec2d(placingX, placingY)), transform, target);
        }
        target.done();
         */
        transform.pop();
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
                StringTextComponent.EMPTY, instance.getConfig(), config -> {
                    processAndSend(new Replace(new PlacedComponent(instance.getType().newInstance(config), pos)));
                }
        );
        if (screen != null) {
            Minecraft.getInstance().displayGuiScreen(screen);
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
