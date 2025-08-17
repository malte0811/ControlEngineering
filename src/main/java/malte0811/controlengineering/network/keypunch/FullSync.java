package malte0811.controlengineering.network.keypunch;

import io.netty.buffer.ByteBuf;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FullSync(int numAvailable, byte[] typed) implements KeypunchSubPacket {
    public static final StreamCodec<ByteBuf, FullSync> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FullSync::numAvailable,
            ByteBufCodecs.BYTE_ARRAY, FullSync::typed,
            FullSync::new
    );

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
