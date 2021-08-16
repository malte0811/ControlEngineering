package malte0811.controlengineering.network.keypunch;

import malte0811.controlengineering.tiles.tape.KeypunchState;
import net.minecraft.network.PacketBuffer;

public class Backspace extends KeypunchSubPacket {
    public Backspace() {}

    public Backspace(PacketBuffer b) {}

    @Override
    protected void write(PacketBuffer out) {}

    @Override
    public boolean process(KeypunchState state) {
        if (state.getData().isEmpty()) {
            return false;
        }
        state.getData().removeByte(state.getData().size() - 1);
        state.setErased(state.getErased() + 1);
        return true;
    }
}
