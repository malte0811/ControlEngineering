package malte0811.controlengineering.network.panellayout;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.panel.PanelDesignScreen;
import malte0811.controlengineering.gui.panel.PanelLayoutContainer;
import malte0811.controlengineering.network.SimplePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class PanelPacket extends SimplePacket {
    private final PanelSubPacket packet;

    public PanelPacket(FriendlyByteBuf buffer) {
        this(PanelSubPacket.read(buffer));
    }

    public PanelPacket(PanelSubPacket data) {
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
            if (activeContainer instanceof PanelLayoutContainer) {
                packet.process(((PanelLayoutContainer) activeContainer).getComponents());
                ((PanelLayoutContainer) activeContainer).sendToListeningPlayersExcept(ctx.getSender(), packet);
                ((PanelLayoutContainer) activeContainer).markDirty();
            }
        } else {
            processOnClient();
        }
    }

    private void processOnClient() {
        PanelDesignScreen currentScreen = StackedScreen.findInstanceOf(PanelDesignScreen.class);
        if (currentScreen != null) {
            packet.process(currentScreen.getComponents());
        }
    }
}
