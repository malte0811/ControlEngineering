package malte0811.controlengineering.gui.panel;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.util.GuiUtil;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PanelDesignScreen extends StackedScreen {
    private static final int BORDER = 20;
    private final List<PlacedComponent> components = new ArrayList<>();

    public PanelDesignScreen() {
        super(new StringTextComponent(""));
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
        final int panelX = selectorWidth + offset;
        PanelLayout panelLayout = new PanelLayout(panelX, BORDER, panelSize, components);
        addButton(panelLayout);
        addButton(new ComponentSelector(offset, BORDER, selectorWidth, panelSize, panelLayout::setPlacingComponent));
    }

    @Override
    protected void renderForeground(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {}

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        final Vec2d mouse = GuiUtil.getMousePosition();
        for (Widget button : buttons) {
            if (button.isMouseOver(mouse.x, mouse.y)) {
                return button.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return false;
    }
}
