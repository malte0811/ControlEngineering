package malte0811.controlengineering.gui;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.chars.Char2CharMap;
import it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap;

public class Keyboard {
    private static final Char2CharMap SPECIAL_UPPERCASE = new Char2CharOpenHashMap();
    public static final KeyRow[] ROWS;
    public static final int CAPSLOCK_ROW = 2;
    public static final double CAPSLOCK_START = -0.25;
    public static final int SPACE_ROW = 4;

    static {
        addSpecialCap('1', '!');
        addSpecialCap('2', '@');
        addSpecialCap('3', '#');
        addSpecialCap('4', '$');
        addSpecialCap('5', '%');
        addSpecialCap('6', '^');
        addSpecialCap('7', '&');
        addSpecialCap('8', '*');
        addSpecialCap('9', '(');
        addSpecialCap('0', ')');
        addSpecialCap('-', '_');
        addSpecialCap('=', '+');
        addSpecialCap('[', '{');
        addSpecialCap(']', '}');
        addSpecialCap('\\', '|');
        addSpecialCap(';', ':');
        addSpecialCap('\'', '"');
        addSpecialCap('.', '<');
        addSpecialCap(',', '>');
        addSpecialCap('/', '?');
        addSpecialCap('`', '~');
        ROWS = new KeyRow[]{
                new KeyRow(0, "`1234567890-="),
                new KeyRow(1.5, "qwertyuiop[]\\"),
                new KeyRow(1.75, "asdfghjkl;'"),
                new KeyRow(2.25, "zxcvbnm,./"),
        };
    }

    private static void addSpecialCap(char lower, char upper) {
        Preconditions.checkArgument(lower <= 0xff);
        Preconditions.checkArgument(upper <= 0xff);
        SPECIAL_UPPERCASE.put(lower, upper);
    }

    public static char upperFor(char in) {
        if (SPECIAL_UPPERCASE.containsKey(in)) {
            return SPECIAL_UPPERCASE.get(in);
        } else {
            return Character.toUpperCase(in);
        }
    }

    public static char convert(char in, boolean shift) {
        if (shift) {
            return upperFor(in);
        } else {
            return in;
        }
    }

    public static class KeyRow {
        public final double relativeStartOffset;
        public final String chars;

        private KeyRow(double relativeStartOffset, String chars) {
            this.relativeStartOffset = relativeStartOffset;
            this.chars = chars;
            for (char c : chars.toCharArray()) {
                Preconditions.checkState(Character.isLowerCase(c) || SPECIAL_UPPERCASE.containsKey(c));
            }
        }
    }
}
