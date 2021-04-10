package malte0811.controlengineering.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.*;

import java.util.List;

public class TextUtil {
    public static final Style GRAY = Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.GRAY));

    public static void addTooltipLine(List<ITextComponent> tooltip, IFormattableTextComponent added) {
        tooltip.add(added.setStyle(GRAY));
    }

    public static void addTooltipLineReordering(List<IReorderingProcessor> tooltip, IFormattableTextComponent added) {
        tooltip.add(added.setStyle(GRAY).func_241878_f());
    }

    public static void renderBoxWithText(
            MatrixStack transform, int color, String text, float scale, int x, int y, int xSize, int ySize
    ) {
        AbstractGui.fill(transform, x, y, x + xSize, y + ySize, color);
        final FontRenderer font = Minecraft.getInstance().fontRenderer;
        final float yOffset = (ySize - font.FONT_HEIGHT / scale) / 2;
        final float xOffset = (xSize - font.getStringWidth(text) / scale) / 2;
        transform.push();
        transform.translate(xOffset + x, y + yOffset, 0);
        transform.scale(1 / scale, 1 / scale, 1);
        final int textColor = 0xff000000 | ColorUtils.inverseColor(color);
        font.drawString(transform, text, 0, 0, textColor);
        transform.pop();
    }
}
