package malte0811.controlengineering.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static malte0811.controlengineering.util.RLUtils.ceLoc;

public interface IPacket extends CustomPacketPayload {
    void process(IPayloadContext context);

    static ServerPlayer serverPlayer(IPayloadContext ctx)
    {
        return (ServerPlayer)ctx.player();
    }

    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String path)
    {
        return new CustomPacketPayload.Type<>(ceLoc(path));
    }
}
