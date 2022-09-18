package malte0811.controlengineering.client;

import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.logic.LogicDesignScreen;
import malte0811.controlengineering.gui.panel.PanelDesignScreen;
import malte0811.controlengineering.gui.scope.ScopeScreen;
import malte0811.controlengineering.gui.tape.ViewTapeScreen;
import malte0811.controlengineering.network.logic.LogicSubPacket;
import malte0811.controlengineering.network.panellayout.PanelSubPacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

/**
 * Client code called from common code; in a separate class for classloading safety
 */
public class ClientHooks {
    public static void openTape(byte[] data, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new ViewTapeScreen("Tape", data, hand));
    }

    public static void processPanelPacketOnClient(PanelSubPacket packet) {
        PanelDesignScreen currentScreen = StackedScreen.findInstanceOf(PanelDesignScreen.class);
        if (currentScreen != null) {
            packet.process(Minecraft.getInstance().level, currentScreen.getComponents());
        }
    }

    public static void processLogicPacketOnClient(LogicSubPacket packet) {
        LogicDesignScreen currentScreen = StackedScreen.findInstanceOf(LogicDesignScreen.class);
        if (currentScreen != null) {
            currentScreen.process(packet);
        }
    }

    public static void processScopePacketOnClient(ScopeSubPacket.IScopeSubPacket packet) {
        ScopeScreen currentScreen = StackedScreen.findInstanceOf(ScopeScreen.class);
        if (currentScreen != null) {
            final var menu = currentScreen.getMenu();
            ScopeSubPacket.processFull(packet, menu.getModules(), menu.getTraces());
        }
    }
}
