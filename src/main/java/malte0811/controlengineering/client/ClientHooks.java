package malte0811.controlengineering.client;

import malte0811.controlengineering.gui.tape.ViewTapeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

/**
 * Client code called from common code; in a separate class for classloading safety
 */
public class ClientHooks {
    public static void openTape(byte[] data, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new ViewTapeScreen("Tape", data, hand));
    }
}
