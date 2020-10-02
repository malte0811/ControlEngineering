package malte0811.controlengineering.util;

public class ColorUtils {
    public static int halfColor(int inColor) {
        return (inColor >>> 1) & 0x7f7f7f7f;
    }

    public static int fractionalColor(int colorIn, double factor) {
        int out = 0;
        for (int i = 0; i < 4; ++i) {
            int shift = i * 8;
            int byteIn = (colorIn >>> shift) & 255;
            out |= ((int) (byteIn * factor)) << shift;
        }
        return out;
    }
}
