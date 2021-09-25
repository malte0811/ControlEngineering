package malte0811.controlengineering.network.keypunch;

import malte0811.controlengineering.tiles.tape.KeypunchState;
import net.minecraft.network.FriendlyByteBuf;

public class FullSync extends KeypunchSubPacket {
    private final int numAvailable;
    private final byte[] typed;

    public FullSync(int numAvailable, byte[] typed) {
        this.numAvailable = numAvailable;
        this.typed = typed;
    }

    public FullSync(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readByteArray());
    }

    @Override
    protected void write(FriendlyByteBuf out) {
        out.writeVarInt(numAvailable);
        out.writeByteArray(typed);
    }

    @Override
    public boolean process(KeypunchState state) {
        state.setAvailable(numAvailable);
        state.getData().clear();
        state.getData().addElements(0, typed);
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
