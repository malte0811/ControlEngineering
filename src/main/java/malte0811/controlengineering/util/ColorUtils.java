package malte0811.controlengineering.util;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

public class ColorUtils {
    public static int halfColor(int inColor) {
        return (inColor >>> 1) & 0x7f7f7f7f;
    }

    public static int fractionalColor(int colorIn, double factor) {
        return applyBytewise(colorIn, i -> (int) (factor * i));
    }

    public static int inverseColor(int colorIn) {
        return applyBytewise(colorIn, i -> 255 - i);
    }

    private static int applyBytewise(int colorIn, Int2IntFunction func) {
        int out = 0;
        for (int i = 0; i < 4; ++i) {
            int shift = i * 8;
            int byteIn = (colorIn >>> shift) & 255;
            out |= func.applyAsInt(byteIn) << shift;
        }
        return out;
    }

    public static float[] unpackColorNoAlpha(int color) {
        return new float[]{
                BitUtils.getBits(color, 24, 8) / 255F,
                BitUtils.getBits(color, 16, 8) / 255F,
                BitUtils.getBits(color, 8, 8) / 255F,
        };
    }
}
