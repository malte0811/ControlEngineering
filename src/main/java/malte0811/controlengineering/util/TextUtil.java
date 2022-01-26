package malte0811.controlengineering.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class TextUtil {
    public static final Style GRAY = Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.GRAY));

    public static void addTooltipLine(List<Component> tooltip, MutableComponent added) {
        tooltip.add(added.setStyle(GRAY));
    }

    public static void addTooltipLineReordering(List<FormattedCharSequence> tooltip, MutableComponent added) {
        tooltip.add(added.setStyle(GRAY).getVisualOrderText());
    }

    public static void renderBoxWithText(
            PoseStack transform, int color, String text, float scale, int x, int y, int xSize, int ySize
    ) {
        GuiComponent.fill(transform, x, y, x + xSize, y + ySize, color);
        final Font font = Minecraft.getInstance().font;
        final float yOffset = (ySize - font.lineHeight / scale) / 2;
        final float xOffset = (xSize - font.width(text) / scale) / 2;
        transform.pushPose();
        transform.translate(xOffset + x, y + yOffset, 0);
        transform.scale(1 / scale, 1 / scale, 1);
        final int textColor = 0xff000000 | ColorUtils.inverseColor(color);
        font.draw(transform, text, 0, 0, textColor);
        transform.popPose();
    }
}
