package malte0811.controlengineering.network.logic;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.client.ClientHooks;
import malte0811.controlengineering.gui.logic.LogicDesignMenu;
import malte0811.controlengineering.network.SimplePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class LogicPacket extends SimplePacket {
    private final LogicSubPacket packet;

    public LogicPacket(FriendlyByteBuf buffer) {
        this(LogicSubPacket.read(buffer));
    }

    public LogicPacket(LogicSubPacket data) {
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
            AbstractContainerMenu activeContainer = ctx.getSender().containerMenu;
            if (activeContainer instanceof LogicDesignMenu logicMenu && !logicMenu.readOnly) {
                packet.process(logicMenu.getSchematic(), $ -> {
                    throw new RuntimeException();
                }, ctx.getSender().level);
                logicMenu.sendToListeningPlayersExcept(ctx.getSender(), packet);
                logicMenu.markDirty();
            }
        } else {
            ClientHooks.processLogicPacketOnClient(packet);
        }
    }
}
