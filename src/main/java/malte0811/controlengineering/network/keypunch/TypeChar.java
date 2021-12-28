package malte0811.controlengineering.network.keypunch;

import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import net.minecraft.network.FriendlyByteBuf;

public class TypeChar extends KeypunchSubPacket {
    private final byte typed;

    public TypeChar(FriendlyByteBuf buffer) {
        typed = buffer.readByte();
    }

    public TypeChar(byte typed) {
        this.typed = typed;
    }

    @Override
    public void write(FriendlyByteBuf out) {
        out.writeByte(typed);
    }

    @Override
    public boolean process(KeypunchState state) {
        return state.tryTypeChar(typed, true);
    }

    @Override
    public void process(ByteConsumer remotePrint) {
        remotePrint.accept(typed);
    }
}
