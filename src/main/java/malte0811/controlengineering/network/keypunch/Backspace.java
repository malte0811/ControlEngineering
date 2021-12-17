package malte0811.controlengineering.network.keypunch;

import malte0811.controlengineering.blockentity.tape.KeypunchState;
import net.minecraft.network.FriendlyByteBuf;

public class Backspace extends KeypunchSubPacket {
    public Backspace() {}

    public Backspace(FriendlyByteBuf b) {}

    @Override
    protected void write(FriendlyByteBuf out) {}

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
