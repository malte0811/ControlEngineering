package malte0811.controlengineering.network.remapper;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.gui.remapper.AbstractRemapperMenu;
import malte0811.controlengineering.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemapperPacket(RemapperSubPacket packet) implements IPacket {
    public static final CustomPacketPayload.Type<RemapperPacket> ID = IPacket.createType("remapper");
    public static final StreamCodec<FriendlyByteBuf, RemapperPacket> CODEC = RemapperSubPacket.CODEC.map(
            RemapperPacket::new, RemapperPacket::packet
    );

    @Override
    public void process(IPayloadContext ctx) {
        AbstractContainerMenu abstractMenu;
        if (ctx.flow().isServerbound()) {
            Preconditions.checkState(packet.allowSendingToServer());
            abstractMenu = ctx.player().containerMenu;
        } else {
            abstractMenu = Minecraft.getInstance().player.containerMenu;
        }
        if (abstractMenu instanceof AbstractRemapperMenu menu) {
            updateConnections(menu);
            if (ctx.flow().isServerbound()) {
                menu.sendToListeningPlayersExcept(IPacket.serverPlayer(ctx), packet);
            }
            menu.markDirty();
        }
    }

    public void updateConnections(AbstractRemapperMenu menu) {
        var newCToG = packet.process(menu.getMapping());
        menu.setMapping(newCToG);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
