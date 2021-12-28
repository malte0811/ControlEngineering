package malte0811.controlengineering.network.keypunch;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.gui.tape.KeypunchContainer;
import malte0811.controlengineering.gui.tape.KeypunchScreen;
import malte0811.controlengineering.network.SimplePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class KeypunchPacket extends SimplePacket {
    private final KeypunchSubPacket packet;

    public KeypunchPacket(FriendlyByteBuf buffer) {
        this(KeypunchSubPacket.read(buffer));
    }

    public KeypunchPacket(KeypunchSubPacket data) {
        this.packet = data;
    }

    @Override
    public void write(FriendlyByteBuf out) {
        packet.writeFull(out);
    }

    @Override
    protected void processOnThread(NetworkEvent.Context ctx) {
        if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            Preconditions.checkState(packet.allowSendingToServer());
            if (ctx.getSender().containerMenu instanceof KeypunchContainer keypunch) {
                if (keypunch.isLoopback()) {
                    packet.process(keypunch.getState());
                    keypunch.sendToListeningPlayersExcept(ctx.getSender(), packet);
                    keypunch.markDirty();
                } else {
                    packet.process(keypunch.getKeypunchBE()::queueForRemotePrint);
                }
            }
        } else {
            processOnClient();
        }
    }

    private void processOnClient() {
        if (Minecraft.getInstance().screen instanceof KeypunchScreen punchScreen) {
            packet.process(punchScreen.getState());
            punchScreen.updateData();
        }
    }
}
