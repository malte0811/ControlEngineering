package malte0811.controlengineering.util;

public class ColorUtils {
    public static int halfColor(int inColor) {
        return (inColor >>> 1) & 0x7f7f7f7f;
    }
}
