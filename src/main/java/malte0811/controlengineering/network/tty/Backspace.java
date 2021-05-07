package malte0811.controlengineering.network.tty;

import malte0811.controlengineering.tiles.tape.TeletypeState;
import net.minecraft.network.PacketBuffer;

public class Backspace extends TTYSubPacket {
    public Backspace() {}

    public Backspace(PacketBuffer b) {}

    @Override
    protected void write(PacketBuffer out) {}

    @Override
    public boolean process(TeletypeState state) {
        if (state.getData().isEmpty()) {
            return false;
        }
        state.getData().removeByte(state.getData().size() - 1);
        state.setErased(state.getErased() + 1);
        return true;
    }
}
