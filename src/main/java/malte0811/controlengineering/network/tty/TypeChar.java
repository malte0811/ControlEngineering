package malte0811.controlengineering.network.tty;

import malte0811.controlengineering.tiles.tape.TeletypeState;
import net.minecraft.network.PacketBuffer;

public class TypeChar extends TTYSubPacket {
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
    public boolean process(TeletypeState state) {
        return state.tryTypeChar(typed);
    }
}
