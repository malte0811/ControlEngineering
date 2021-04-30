package malte0811.controlengineering.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ColorPicker16 extends Widget {
    public static final int GRID_SIZE = 16;
    public static final int NUM_COLS = 4;
    public static final int SIZE = NUM_COLS * GRID_SIZE;
    public static final int TITLE_SPACE = 10;

    private DyeColor selected;

    public ColorPicker16(int x, int y, ITextComponent title, @Nullable DyeColor initial) {
        super(x, y, SIZE, SIZE + TITLE_SPACE, title);
        selected = initial;
    }

    @Override
    public void renderWidget(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        final FontRenderer font = Minecraft.getInstance().fontRenderer;
        final float offset = (width - font.getStringPropertyWidth(getMessage())) / 2f;
        font.drawText(matrixStack, getMessage(), x + offset, y, -1);
        DyeColor underCursor = getColorUnderCursor(mouseX, mouseY);
        for (DyeColor color : DyeColor.values()) {
            final int minX = x + (color.getId() % NUM_COLS) * GRID_SIZE;
            final int minY = y + (color.getId() / NUM_COLS) * GRID_SIZE + TITLE_SPACE;
            int border = 2;
            if (underCursor == color) {
                final int inverse = ColorUtils.inverseColor(color.getColorValue());
                fill(matrixStack, minX, minY, minX + GRID_SIZE, minY + GRID_SIZE, inverse);
                if (selected == color) {
                    border = 1;
                }
            } else if (selected == color) {
                border = 0;
            }
            fill(
                    matrixStack,
                    minX + border, minY + border,
                    minX + GRID_SIZE - border, minY + GRID_SIZE - border,
                    0xff000000 | color.getColorValue()
            );
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        final DyeColor selected = getColorUnderCursor(mouseX, mouseY);
        if (selected != null) {
            if (selected == this.selected) {
                this.selected = null;
            } else {
                this.selected = selected;
            }
        }
    }

    @Nullable
    public DyeColor getSelected() {
        return selected;
    }

    @Nullable
    private DyeColor getColorUnderCursor(double mouseX, double mouseY) {
        mouseX -= x;
        mouseY -= y + TITLE_SPACE;
        final int row = (int) (mouseY / GRID_SIZE);
        final int col = (int) (mouseX / GRID_SIZE);
        if (row < 0 || row >= NUM_COLS || col < 0 || col >= NUM_COLS) {
            return null;
        }
        final int index = row * NUM_COLS + col;
        return DyeColor.byId(index);
    }
}
