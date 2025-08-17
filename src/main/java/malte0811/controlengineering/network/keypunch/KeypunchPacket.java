package malte0811.controlengineering.network.keypunch;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.gui.tape.KeypunchMenu;
import malte0811.controlengineering.gui.tape.KeypunchScreen;
import malte0811.controlengineering.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KeypunchPacket(KeypunchSubPacket packet) implements IPacket {
    public static final CustomPacketPayload.Type<KeypunchPacket> ID = IPacket.createType("keypunch");
    public static final StreamCodec<FriendlyByteBuf, KeypunchPacket> CODEC = KeypunchSubpackets.STREAM_CODEC.map(
            KeypunchPacket::new, KeypunchPacket::packet
    );

    @Override
    public void process(IPayloadContext ctx) {
        if (ctx.flow().isServerbound()) {
            Preconditions.checkState(packet.allowSendingToServer());
            if (ctx.player().containerMenu instanceof KeypunchMenu keypunch) {
                if (keypunch.isLoopback()) {
                    packet.process(keypunch.getState());
                    keypunch.sendToListeningPlayersExcept(IPacket.serverPlayer(ctx), packet);
                    keypunch.markDirty();
                } else {
                    packet.process(keypunch.getPrintNonLoopback());
                }
            }
        } else {
            processOnClient();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    private void processOnClient() {
        if (Minecraft.getInstance().screen instanceof KeypunchScreen punchScreen) {
            packet.process(punchScreen.getState());
            punchScreen.updateData();
        }
    }
}
