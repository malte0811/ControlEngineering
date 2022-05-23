package malte0811.controlengineering.network.remapper;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.gui.remapper.AbstractRemapperMenu;
import malte0811.controlengineering.network.SimplePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class RemapperPacket extends SimplePacket {
    private final RemapperSubPacket packet;

    public RemapperPacket(FriendlyByteBuf buffer) {
        this(RemapperSubPacket.read(buffer));
    }

    public RemapperPacket(RemapperSubPacket data) {
        this.packet = data;
    }

    @Override
    public void write(FriendlyByteBuf out) {
        packet.writeFull(out);
    }

    @Override
    protected void processOnThread(NetworkEvent.Context ctx) {
        AbstractContainerMenu abstractMenu;
        if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            Preconditions.checkState(packet.allowSendingToServer());
            abstractMenu = ctx.getSender().containerMenu;
        } else {
            abstractMenu = Minecraft.getInstance().player.containerMenu;
        }
        if (abstractMenu instanceof AbstractRemapperMenu menu) {
            updateConnections(menu);
            if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                menu.sendToListeningPlayersExcept(ctx.getSender(), packet);
            }
            menu.markDirty();
        }
    }

    public void updateConnections(AbstractRemapperMenu menu) {
        var newCToG = packet.process(menu.getMapping());
        menu.setMapping(newCToG);
    }
}
