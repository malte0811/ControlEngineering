package malte0811.controlengineering.network.keypunch;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.gui.tape.KeypunchContainer;
import malte0811.controlengineering.gui.tape.KeypunchScreen;
import malte0811.controlengineering.network.SimplePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class KeypunchPacket extends SimplePacket {
    private final KeypunchSubPacket packet;

    public KeypunchPacket(PacketBuffer buffer) {
        this(KeypunchSubPacket.read(buffer));
    }

    public KeypunchPacket(KeypunchSubPacket data) {
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
            if (activeContainer instanceof KeypunchContainer) {
                KeypunchContainer ttyContainer = (KeypunchContainer) activeContainer;
                packet.process(ttyContainer.getState());
                ttyContainer.sendToListeningPlayersExcept(ctx.getSender(), packet);
                ttyContainer.markDirty();
            }
        } else {
            processOnClient();
        }
    }

    private void processOnClient() {
        Screen openScreen = Minecraft.getInstance().currentScreen;
        if (openScreen instanceof KeypunchScreen) {
            packet.process(((KeypunchScreen) openScreen).getState());
            ((KeypunchScreen) openScreen).updateData();
        }
    }
}
