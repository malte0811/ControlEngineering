package malte0811.controlengineering.network.logic;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.logic.LogicDesignContainer;
import malte0811.controlengineering.gui.logic.LogicDesignScreen;
import malte0811.controlengineering.network.SimplePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class LogicPacket extends SimplePacket {
    private final LogicSubPacket packet;

    public LogicPacket(PacketBuffer buffer) {
        this(LogicSubPacket.read(buffer));
    }

    public LogicPacket(LogicSubPacket data) {
        this.packet = data;
    }

    @Override
    public void write(PacketBuffer out) {
        packet.writeFull(out);
    }

    @Override
    protected void processOnThread(NetworkEvent.Context ctx) {
        if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            Preconditions.checkState(packet.allowSendingToServer());
            Container activeContainer = ctx.getSender().openContainer;
            if (activeContainer instanceof LogicDesignContainer) {
                packet.process(((LogicDesignContainer) activeContainer).getSchematic(), $ -> {
                    throw new RuntimeException();
                });
                ((LogicDesignContainer) activeContainer).sendToListeningPlayersExcept(ctx.getSender(), packet);
            }
        } else {
            processOnClient();
        }
    }

    private void processOnClient() {
        Screen currentScreen = Minecraft.getInstance().currentScreen;
        if (!(currentScreen instanceof StackedScreen)) {
            return;
        }
        while (currentScreen != null && !(currentScreen instanceof LogicDesignScreen)) {
            currentScreen = ((StackedScreen) currentScreen).getPreviousInStack();
        }
        if (currentScreen != null) {
            packet.process(
                    ((LogicDesignScreen) currentScreen).getSchematic(),
                    ((LogicDesignScreen) currentScreen)::setSchematic
            );
        }
    }
}
