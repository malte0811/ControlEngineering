package malte0811.controlengineering.util;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;

public class BitUtils {
    public static int getBits(int value, int offset, int bits) {
        int mask = (1 << bits) - 1;
        return (value >>> offset) & mask;
    }

    public static int lowestNBits(int value, int numBits) {
        return value & ((1 << numBits) - 1);
    }

    public static boolean getBit(int value, int bit) {
        return getBits(value, bit, 1) != 0;
    }

    public static boolean calculateParityBit(byte data) {
        return (getSetBits(data & 0x7f) % 2) == 1;
    }

    public static boolean noBitsAbove(int value, int maxAllowedBit) {
        return (value >>> (maxAllowedBit + 1)) == 0;
    }

    public static boolean isCorrectParity(byte data) {
        return getBit(data, 7) == calculateParityBit(data);
    }

    public static int getSetBits(int v) {
        return Integer.bitCount(v);
    }

    public static ByteList toBytesWithParity(String in) {
        for (int i = 0; i < in.length(); ++i) {
            Preconditions.checkArgument(isASCIICharacter(in.codePointAt(i)));
        }
        byte[] result = in.getBytes();
        for (int i = 0; i < result.length; ++i) {
            result[i] = fixParity(result[i]);
        }
        return new ByteArrayList(result);
    }

    public static byte clearParity(byte withParity) {
        return (byte) (withParity & 0x7f);
    }

    public static byte fixParity(byte in) {
        if (calculateParityBit(in)) {
            return (byte) (in | 0x80);
        } else {
            return clearParity(in);
        }
    }

    public static boolean isASCIICharacter(int in) {
        return noBitsAbove(in, 6);
    }

    public static String toString(ByteList withParity) {
        byte[] copyWithoutParity = new byte[withParity.size()];
        for (int i = 0; i < withParity.size(); ++i) {
            if ((withParity.getByte(i) & 0xff) != 0xff) {
                copyWithoutParity[i] = clearParity(withParity.getByte(i));
            }
        }
        return new String(copyWithoutParity);
    }
}
