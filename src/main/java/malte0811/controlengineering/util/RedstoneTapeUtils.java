package malte0811.controlengineering.util;

import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

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

    public static int getRSColor(float strength) {
        // Based on the static initializer for RedstoneWireBlock#powerRGB
        final float red = strength * 0.6F + (strength > 0.0F ? 0.4F : 0.3F);
        final float green = Mth.clamp(strength * strength * 0.7F - 0.5F, 0.0F, 1.0F);
        final float blue = Mth.clamp(strength * strength * 0.6F - 0.7F, 0.0F, 1.0F);
        return 0xff000000 | Mth.color(red, green, blue);
    }
}
