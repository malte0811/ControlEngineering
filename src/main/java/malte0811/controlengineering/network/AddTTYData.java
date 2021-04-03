package malte0811.controlengineering.network;

import malte0811.controlengineering.gui.tape.TeletypeContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;

public class AddTTYData extends SimplePacket {
    private final byte[] typed;

    public AddTTYData(PacketBuffer buffer) {
        typed = buffer.readByteArray();
    }

    public AddTTYData(byte[] typed) {
        this.typed = typed;
    }

    @Override
    public void write(PacketBuffer out) {
        out.writeByteArray(typed);
    }

    @Override
    protected void processOnThread(NetworkEvent.Context ctx) {
        Container open = Objects.requireNonNull(ctx.getSender()).openContainer;
        if (open instanceof TeletypeContainer) {
            ((TeletypeContainer) open).typeAll(typed);
            //TODO send to other players?
        }
    }
}
