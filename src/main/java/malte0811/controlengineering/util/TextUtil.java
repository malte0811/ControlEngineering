package malte0811.controlengineering.util;

import net.minecraft.util.text.*;

import java.util.List;

public class TextUtil {
    public static final Style GRAY = Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.GRAY));

    public static void addTooltipLine(List<ITextComponent> tooltip, IFormattableTextComponent added) {
        tooltip.add(added.setStyle(GRAY));
    }
}
