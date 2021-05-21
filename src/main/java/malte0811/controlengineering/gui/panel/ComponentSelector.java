package malte0811.controlengineering.gui.panel;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ComponentSelector extends Widget {
    private static final int ROW_MIN_HEIGHT = 40;
    private static final int BUTTON_HEIGHT = 20;

    private final Minecraft mc;
    private final List<PanelComponentType<?, ?>> available;
    private final int numCols = 2;
    private final int numRows;
    private final int actualRowHeight;
    private final int colWidth;
    private final int numPages;
    private final List<Widget> buttons;
    private final Consumer<PanelComponentType<?, ?>> select;

    private int page = 0;

    public ComponentSelector(int x, int y, int width, int height, Consumer<PanelComponentType<?, ?>> select) {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.mc = Minecraft.getInstance();
        this.select = select;
        this.available = new ArrayList<>(PanelComponents.REGISTRY.getValues());
        final int displayHeight = height - BUTTON_HEIGHT;
        this.numRows = Math.max(displayHeight / ROW_MIN_HEIGHT, 1);
        this.actualRowHeight = displayHeight / numRows;
        this.colWidth = width / numCols;
        this.numPages = MathHelper.ceil(available.size() / (double) (numRows * numCols));
        this.buttons = ImmutableList.of(
                new Button(
                        x, y + displayHeight, width / 3, BUTTON_HEIGHT,
                        new StringTextComponent("<-"), $ -> page = Math.max(0, page - 1)
                ),
                new Button(
                        x + 2 * width / 3, y + displayHeight, width / 3, BUTTON_HEIGHT,
                        new StringTextComponent("->"), $ -> page = Math.min(numPages - 1, page + 1)
                )
        );
    }

    @Override
    public void renderWidget(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (Widget w : buttons) {
            w.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        drawCenteredString(
                matrixStack, mc.fontRenderer, (page + 1) + " / " + numPages,
                x + width / 2, y + height - (BUTTON_HEIGHT + mc.fontRenderer.FONT_HEIGHT) / 2, -1
        );
        final int selectedRow = (mouseY - y) / actualRowHeight;
        final int selectedCol = (mouseX - x) / colWidth;
        for (int col = 0; col < numCols; ++col) {
            for (int row = 0; row < numRows; ++row) {
                renderAvailableType(
                        matrixStack, getTypeIn(row, col),
                        x + col * colWidth, y + row * actualRowHeight,
                        col == selectedCol && row == selectedRow
                );
            }
        }
    }

    private <C, S> void renderAvailableType(
            @Nonnull MatrixStack transform, @Nullable PanelComponentType<C, S> type, int x, int y, boolean highlight
    ) {
        transform.push();
        transform.translate(x, y, 0);
        if (highlight && type != null) {
            fill(transform, 0, 0, colWidth, actualRowHeight, 0xffaaaaff);
        } else {
            fill(transform, 0, 0, colWidth, actualRowHeight, 0xffaaaaaa);
        }
        if (type != null) {
            transform.translate(0, 1, 0);
            String name = I18n.format(type.getTranslationKey());
            mc.fontRenderer.drawString(transform, name, (colWidth - mc.fontRenderer.getStringWidth(name)) / 2f, 0, 0);

            transform.translate(colWidth / 2., (actualRowHeight + mc.fontRenderer.FONT_HEIGHT + 10) / 2., 0);
            transform.scale(16, 16, 1);
            transform.rotate(new Quaternion(30, 45, 180, true));
            mc.textureManager.bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
            //TODO GuiRenderTarget.renderSingleComponent(type.newInstance(), transform);
        }

        transform.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseY < y || mouseX > x + width || mouseY > y + height) {
            return false;
        }
        for (Widget w : buttons) {
            if (w.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        final int row = (int) ((mouseY - y) / actualRowHeight);
        final int col = (int) ((mouseX - x) / colWidth);
        if (row >= 0 && row < numRows && col >= 0 && col < numCols) {
            PanelComponentType<?, ?> type = getTypeIn(row, col);
            if (type != null) {
                select.accept(type);
                return true;
            }
        }
        return false;
    }

    @Nullable
    private PanelComponentType<?, ?> getTypeIn(int row, int col) {
        final int index = page * numRows * numCols + row * numCols + col;
        if (index < 0 || index >= available.size()) {
            return null;
        } else {
            return available.get(index);
        }
    }
}
