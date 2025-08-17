package malte0811.controlengineering.network.scope;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.client.ClientHooks;
import malte0811.controlengineering.gui.scope.ScopeMenu;
import malte0811.controlengineering.network.IPacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket.IScopeSubPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ScopePacket(IScopeSubPacket packet) implements IPacket {
    public static final CustomPacketPayload.Type<ScopePacket> ID = IPacket.createType("scope");
    public static final StreamCodec<FriendlyByteBuf, ScopePacket> CODEC = ScopeSubPacket.CODEC.map(
            ScopePacket::new, ScopePacket::packet
    );

    @Override
    public void process(IPayloadContext ctx) {
        if (ctx.flow().isServerbound()) {
            Preconditions.checkState(packet.allowSendingToServer());
            AbstractContainerMenu activeContainer = ctx.player().containerMenu;
            if (!(activeContainer instanceof ScopeMenu scopeMenu)) {
                return;
            }
            ScopeSubPacket.processFull(packet, scopeMenu);
            scopeMenu.sendToListeningPlayersExcept(IPacket.serverPlayer(ctx), packet);
            scopeMenu.markDirty();
        } else {
            ClientHooks.processScopePacketOnClient(packet);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
