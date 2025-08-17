package malte0811.controlengineering.network.remapper;

import io.netty.buffer.ByteBuf;
import malte0811.controlengineering.gui.remapper.AbstractRemapperMenu;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SetMapping(int colorIndex, int grayIndex) implements RemapperSubPacket {
    public static final StreamCodec<ByteBuf, SetMapping> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetMapping::colorIndex,
            ByteBufCodecs.VAR_INT, SetMapping::grayIndex,
            SetMapping::new
    );

    @Override
    public int[] process(int[] colorToGray) {
        for (int i = 0; i < colorToGray.length; ++i) {
            if (colorToGray[i] == grayIndex) {
                colorToGray[i] = AbstractRemapperMenu.NOT_MAPPED;
            }
        }
        colorToGray[colorIndex] = grayIndex;
        return colorToGray;
    }
}
