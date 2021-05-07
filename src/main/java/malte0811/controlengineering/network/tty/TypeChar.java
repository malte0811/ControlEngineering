package malte0811.controlengineering.network.tty;

import malte0811.controlengineering.tiles.tape.TeletypeState;
import malte0811.controlengineering.util.BitUtils;
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
        if (state.getAvailable() <= 0) {
            return false;
        }
        state.setAvailable(state.getAvailable() - 1);
        state.getData().add(BitUtils.fixParity(typed));
        return true;
    }
}
