package malte0811.controlengineering.network.tty;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.gui.tape.TeletypeContainer;
import malte0811.controlengineering.gui.tape.TeletypeScreen;
import malte0811.controlengineering.network.SimplePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class TTYPacket extends SimplePacket {
    private final TTYSubPacket packet;

    public TTYPacket(PacketBuffer buffer) {
        this(TTYSubPacket.read(buffer));
    }

    public TTYPacket(TTYSubPacket data) {
        this.packet = data;
    }

    @Override
    public void write(PacketBuffer out) {
        packet.writeFull(out);
    }

    @Override
    protected void processOnThread(NetworkEvent.Context ctx) {
        if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            Preconditions.checkState(packet.allowSendingToServer());
            Container activeContainer = ctx.getSender().openContainer;
            if (activeContainer instanceof TeletypeContainer) {
                packet.process(((TeletypeContainer) activeContainer).getState());
                ((TeletypeContainer) activeContainer).sendToListeningPlayersExcept(ctx.getSender(), packet);
            }
        } else {
            processOnClient();
        }
    }

    private void processOnClient() {
        Screen openScreen = Minecraft.getInstance().currentScreen;
        if (openScreen instanceof TeletypeScreen) {
            packet.process(((TeletypeScreen) openScreen).getState());
        }
    }
}
