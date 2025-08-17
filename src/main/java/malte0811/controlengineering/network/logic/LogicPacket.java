package malte0811.controlengineering.network.logic;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.client.ClientHooks;
import malte0811.controlengineering.gui.logic.LogicDesignMenu;
import malte0811.controlengineering.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LogicPacket(LogicSubPacket packet) implements IPacket {
    public static final CustomPacketPayload.Type<LogicPacket> ID = IPacket.createType("logic");
    public static final StreamCodec<FriendlyByteBuf, LogicPacket> CODEC = LogicSubPackets.CODEC.map(
            LogicPacket::new, LogicPacket::packet
    );

    @Override
    public void process(IPayloadContext ctx) {
        if (ctx.flow().isServerbound()) {
            Preconditions.checkState(packet.allowSendingToServer());
            AbstractContainerMenu activeContainer = ctx.player().containerMenu;
            if (!(activeContainer instanceof LogicDesignMenu logicMenu)) {
                return;
            }
            if (!logicMenu.readOnly || packet.canApplyOnReadOnly()) {
                packet.process(logicMenu.getSchematic(), $ -> {
                    throw new RuntimeException();
                }, ctx.player().level());
                logicMenu.sendToListeningPlayersExcept(IPacket.serverPlayer(ctx), packet);
                logicMenu.markDirty();
            }
        } else {
            ClientHooks.processLogicPacketOnClient(packet);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
