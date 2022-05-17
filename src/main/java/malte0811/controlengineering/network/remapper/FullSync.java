package malte0811.controlengineering.network.remapper;

import net.minecraft.network.FriendlyByteBuf;

public class FullSync extends RemapperSubPacket {
    private final int[] colorToGray;

    public FullSync(int[] colorToGray) {
        this.colorToGray = colorToGray;
    }

    public FullSync(FriendlyByteBuf in) {
        this(in.readVarIntArray());
    }

    @Override
    protected void write(FriendlyByteBuf out) {
        out.writeVarIntArray(colorToGray);
    }

    @Override
    protected int[] process(int[] colorToGray) {
        return this.colorToGray;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
