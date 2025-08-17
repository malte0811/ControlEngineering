package malte0811.controlengineering.network.remapper;

import io.netty.buffer.ByteBuf;
import malte0811.controlengineering.gui.remapper.AbstractRemapperMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClearMapping(int colorIndex) implements RemapperSubPacket {
    public static final StreamCodec<ByteBuf, ClearMapping> CODEC = ByteBufCodecs.VAR_INT.map(
            ClearMapping::new, ClearMapping::colorIndex
    );

    @Override
    public int[] process(int[] colorToGray) {
        colorToGray[colorIndex] = AbstractRemapperMenu.NOT_MAPPED;
        return colorToGray;
    }
}
