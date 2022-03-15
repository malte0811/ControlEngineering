package malte0811.controlengineering.network.remapper;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.gui.remapper.RSRemapperMenu;
import malte0811.controlengineering.network.SimplePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class RSRemapperPacket extends SimplePacket {
    private final RSRemapperSubPacket packet;

    public RSRemapperPacket(FriendlyByteBuf buffer) {
        this(RSRemapperSubPacket.read(buffer));
    }

    public RSRemapperPacket(RSRemapperSubPacket data) {
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
        if (abstractMenu instanceof RSRemapperMenu menu) {
            updateConnections(menu);
            if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                menu.sendToListeningPlayersExcept(ctx.getSender(), packet);
            }
            menu.markDirty();
        }
    }

    public void updateConnections(RSRemapperMenu menu) {
        var newCToG = packet.process(menu.getColorToGray());
        menu.setColorToGray(newCToG);
    }
}
