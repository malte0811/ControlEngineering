package malte0811.controlengineering.network.keypunch;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TypeChar(byte typed) implements KeypunchSubPacket {
    public static final StreamCodec<ByteBuf, TypeChar> CODEC = ByteBufCodecs.BYTE.map(
            TypeChar::new, TypeChar::typed
    );

    @Override
    public boolean process(KeypunchState state) {
        return state.tryTypeChar(typed, true);
    }

    @Override
    public void process(ByteConsumer remotePrint) {
        remotePrint.accept(typed);
    }
}
