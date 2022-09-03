package malte0811.controlengineering.network.scope;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.client.ClientHooks;
import malte0811.controlengineering.gui.scope.ScopeMenu;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket.IScopeSubPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class ScopePacket extends SimplePacket {
    private final IScopeSubPacket packet;

    public ScopePacket(FriendlyByteBuf buffer) {
        this(ScopeSubPacket.read(buffer));
    }

    public ScopePacket(IScopeSubPacket data) {
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
            if (!(activeContainer instanceof ScopeMenu scopeMenu)) {
                return;
            }
            packet.process(scopeMenu.getModules());
            scopeMenu.sendToListeningPlayersExcept(ctx.getSender(), packet);
            scopeMenu.markDirty();
        } else {
            ClientHooks.processScopePacketOnClient(packet);
        }
    }
}
