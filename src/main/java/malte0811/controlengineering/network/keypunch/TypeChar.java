package malte0811.controlengineering.network.keypunch;

import malte0811.controlengineering.tiles.tape.KeypunchState;
import net.minecraft.network.PacketBuffer;

public class TypeChar extends KeypunchSubPacket {
    private final byte typed;

    public TypeChar(PacketBuffer buffer) {
        typed = buffer.readByte();
    }

    public TypeChar(byte typed) {
        this.typed = typed;
    }

    @Override
    public void write(PacketBuffer out) {
        out.writeByte(typed);
    }

    @Override
    public boolean process(KeypunchState state) {
        return state.tryTypeChar(typed);
    }
}
