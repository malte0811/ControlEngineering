package malte0811.controlengineering.network.keypunch;

import malte0811.controlengineering.blockentity.tape.KeypunchState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record Backspace() implements KeypunchSubPacket {
    public static final StreamCodec<FriendlyByteBuf, Backspace> CODEC = StreamCodec.unit(new Backspace());

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
