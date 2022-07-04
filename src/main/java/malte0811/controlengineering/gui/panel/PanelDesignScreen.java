package malte0811.controlengineering.gui.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.util.ScreenUtils;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.List;

public class PanelDesignScreen extends StackedScreen implements MenuAccess<PanelDesignMenu> {
    public static final String REQUIRED_VS_AVAILABLE_TAPE = ControlEngineering.MODID + ".gui.reqVsAvTape";
    private static final int BORDER = 20;
    @Nonnull
    private final PanelDesignMenu container;
    private int panelLayoutXMin;
    private int panelLayoutYMax;

    public PanelDesignScreen(@Nonnull PanelDesignMenu container, Component title) {
        super(title);
        this.container = container;
    }

    @Override
    protected void init() {
        super.init();
        final int usedHeight = height - 2 * BORDER;
        final int availableWidth = width - 2 * BORDER;
        final int selectorWidth = Mth.clamp(availableWidth - usedHeight, 100, usedHeight / 2);
        final int panelSize = Mth.clamp(usedHeight, 150, availableWidth - selectorWidth);
        final int usedWidth = selectorWidth + panelSize;
        final int offset = (width - usedWidth) / 2;
        this.panelLayoutXMin = selectorWidth + offset;
        this.panelLayoutYMax = BORDER + panelSize;
        PanelLayout panelLayout = new PanelLayout(panelLayoutXMin, BORDER, panelSize, container.getComponents());
        addRenderableWidget(new ComponentSelector(offset, BORDER, selectorWidth, panelSize, panelLayout::setPlacingComponent));
        addRenderableWidget(panelLayout);
    }

    @Override
    protected void renderForeground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        final int required = container.getRequiredTapeLength();
        final int available = container.getAvailableTapeLength();
        final int color = required <= available ? -1 : 0xff_ff0000;
        Minecraft.getInstance().font.draw(
                matrixStack,
                Component.translatable(REQUIRED_VS_AVAILABLE_TAPE, required, available),
                panelLayoutXMin, panelLayoutYMax + 5,
                color
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        final Vec2d mouse = ScreenUtils.getMousePosition();
        for (var button : children()) {
            if (button.isMouseOver(mouse.x(), mouse.y())) {
                if (button.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                } else {
                    break;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public List<PlacedComponent> getComponents() {
        return container.getComponents();
    }

    @Nonnull
    @Override
    public PanelDesignMenu getMenu() {
        return container;
    }
}
