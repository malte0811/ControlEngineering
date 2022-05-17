package malte0811.controlengineering.network.remapper;

import malte0811.controlengineering.gui.remapper.AbstractRemapperMenu;
import net.minecraft.network.FriendlyByteBuf;

public class ClearMapping extends RemapperSubPacket {
    private final int colorIndex;

    public ClearMapping(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public ClearMapping(FriendlyByteBuf in) {
        this(in.readVarInt());
    }

    @Override
    protected void write(FriendlyByteBuf out) {
        out.writeVarInt(colorIndex);
    }

    @Override
    protected int[] process(int[] colorToGray) {
        colorToGray[colorIndex] = AbstractRemapperMenu.NOT_MAPPED;
        return colorToGray;
    }
}
