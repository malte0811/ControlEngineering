package malte0811.controlengineering.util;

import net.minecraft.item.DyeColor;

public class RedstoneTapeUtils {
    public static int getStrength(byte b) {
        return (b >>> 4) & 0xf;
    }

    public static DyeColor getColor(byte b) {
        return DyeColor.byId(b & 0xf);
    }

    public static byte combine(DyeColor color, int strength) {
        return (byte) ((strength << 4) | color.getId());
    }
}
