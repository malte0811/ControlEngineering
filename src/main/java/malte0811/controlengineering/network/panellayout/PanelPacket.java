package malte0811.controlengineering.network.panellayout;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.client.ClientHooks;
import malte0811.controlengineering.gui.panel.PanelDesignMenu;
import malte0811.controlengineering.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PanelPacket(PanelSubPacket packet) implements IPacket {
    public static final CustomPacketPayload.Type<PanelPacket> ID = IPacket.createType("panel");
    public static final StreamCodec<FriendlyByteBuf, PanelPacket> CODEC = PanelSubPackets.CODEC.map(
            PanelPacket::new, PanelPacket::packet
    );

    @Override
    public void process(IPayloadContext ctx) {
        if (ctx.flow().isServerbound()) {
            Preconditions.checkState(packet.allowSendingToServer());
            AbstractContainerMenu activeContainer = ctx.player().containerMenu;
            if (activeContainer instanceof PanelDesignMenu panelContainer) {
                packet.process(ctx.player().level(), panelContainer.getComponents());
                panelContainer.sendToListeningPlayersExcept(IPacket.serverPlayer(ctx), packet);
                panelContainer.markDirty();
            }
        } else {
            ClientHooks.processPanelPacketOnClient(packet);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
