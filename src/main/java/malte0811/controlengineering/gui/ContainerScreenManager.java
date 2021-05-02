package malte0811.controlengineering.gui;

import malte0811.controlengineering.gui.logic.LogicDesignContainer;
import malte0811.controlengineering.gui.logic.LogicDesignScreen;
import malte0811.controlengineering.gui.panel.PanelDesignScreen;
import malte0811.controlengineering.gui.panel.PanelLayoutContainer;
import malte0811.controlengineering.gui.tape.TeletypeContainer;
import malte0811.controlengineering.gui.tape.TeletypeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerScreenManager {
    // IDEA considers the type arguments to be redundant, but the compiler disagrees, and that's the thing that
    // actually *needs* to like my code, so it wins
    @SuppressWarnings("RedundantTypeArguments")
    public static void registerScreens() {
        ScreenManager.<TeletypeContainer, TeletypeScreen>registerFactory(
                CEContainers.TELETYPE.get(), (container, inv, title) -> new TeletypeScreen(container, title)
        );
        ScreenManager.<LogicDesignContainer, LogicDesignScreen>registerFactory(
                CEContainers.LOGIC_DESIGN.get(), (container, inv, title) -> new LogicDesignScreen(container, title)
        );
        ScreenManager.<PanelLayoutContainer, PanelDesignScreen>registerFactory(
                CEContainers.PANEL_LAYOUT.get(), (container, inv, title) -> new PanelDesignScreen(container, title)
        );
    }

    public static IWorldPosCallable readWorldPos(PacketBuffer buffer) {
        if (buffer == null) {
            return IWorldPosCallable.DUMMY;
        }
        BlockPos pos = buffer.readBlockPos();
        World world = Minecraft.getInstance().world;
        if (world == null) {
            return IWorldPosCallable.DUMMY;
        }
        return IWorldPosCallable.of(world, pos);
    }
}
