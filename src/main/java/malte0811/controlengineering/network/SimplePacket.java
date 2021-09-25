package malte0811.controlengineering.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class SimplePacket {
    public abstract void write(FriendlyByteBuf out);

    protected abstract void processOnThread(NetworkEvent.Context ctx);

    public final void process(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> processOnThread(ctx));
        ctx.setPacketHandled(true);
    }
}
