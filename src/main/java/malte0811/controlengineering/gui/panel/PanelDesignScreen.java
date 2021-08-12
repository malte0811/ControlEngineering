package malte0811.controlengineering.gui.panel;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.util.GuiUtil;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class PanelDesignScreen extends StackedScreen implements IHasContainer<PanelLayoutContainer> {
    public static final String REQUIRED_VS_AVAILABLE_TAPE = ControlEngineering.MODID + ".gui.reqVsAvTape";
    private static final int BORDER = 20;
    @Nonnull
    private final PanelLayoutContainer container;
    private int panelLayoutXMin;
    private int panelLayoutYMax;

    public PanelDesignScreen(@Nonnull PanelLayoutContainer container, ITextComponent title) {
        super(title);
        this.container = container;
    }

    @Override
    protected void init() {
        super.init();
        final int usedHeight = height - 2 * BORDER;
        final int availableWidth = width - 2 * BORDER;
        final int selectorWidth = MathHelper.clamp(availableWidth - usedHeight, 100, usedHeight / 2);
        final int panelSize = MathHelper.clamp(usedHeight, 150, availableWidth - selectorWidth);
        final int usedWidth = selectorWidth + panelSize;
        final int offset = (width - usedWidth) / 2;
        this.panelLayoutXMin = selectorWidth + offset;
        this.panelLayoutYMax = BORDER + panelSize;
        PanelLayout panelLayout = new PanelLayout(panelLayoutXMin, BORDER, panelSize, container.getComponents());
        addButton(panelLayout);
        addButton(new ComponentSelector(offset, BORDER, selectorWidth, panelSize, panelLayout::setPlacingComponent));
    }

    @Override
    protected void renderForeground(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        final int required = container.getRequiredTapeLength();
        final int available = container.getAvailableTapeLength();
        final int color = required <= available ? -1 : 0xff_ff0000;
        Minecraft.getInstance().fontRenderer.drawText(
                matrixStack,
                new TranslationTextComponent(REQUIRED_VS_AVAILABLE_TAPE, required, available),
                panelLayoutXMin, panelLayoutYMax + 5,
                color
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        final Vec2d mouse = GuiUtil.getMousePosition();
        for (Widget button : buttons) {
            if (button.isMouseOver(mouse.x, mouse.y)) {
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
    public PanelLayoutContainer getContainer() {
        return container;
    }
}
