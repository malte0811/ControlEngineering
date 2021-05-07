package malte0811.controlengineering.network.tty;

import malte0811.controlengineering.tiles.tape.TeletypeState;
import net.minecraft.network.PacketBuffer;

public class FullSync extends TTYSubPacket {
    private final int numAvailable;
    private final byte[] typed;

    public FullSync(int numAvailable, byte[] typed) {
        this.numAvailable = numAvailable;
        this.typed = typed;
    }

    public FullSync(PacketBuffer buffer) {
        this(buffer.readVarInt(), buffer.readByteArray());
    }

    @Override
    protected void write(PacketBuffer out) {
        out.writeVarInt(numAvailable);
        out.writeByteArray(typed);
    }

    @Override
    public boolean process(TeletypeState state) {
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
