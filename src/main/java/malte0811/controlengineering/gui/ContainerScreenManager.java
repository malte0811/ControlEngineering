package malte0811.controlengineering.gui;

import malte0811.controlengineering.gui.logic.LogicDesignContainer;
import malte0811.controlengineering.gui.logic.LogicDesignScreen;
import malte0811.controlengineering.gui.panel.PanelDesignScreen;
import malte0811.controlengineering.gui.panel.PanelDesignContainer;
import malte0811.controlengineering.gui.tape.KeypunchContainer;
import malte0811.controlengineering.gui.tape.KeypunchScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;

public class ContainerScreenManager {
    // IDEA considers the type arguments to be redundant, but the compiler disagrees, and that's the thing that
    // actually *needs* to like my code, so it wins
    @SuppressWarnings("RedundantTypeArguments")
    public static void registerScreens() {
        MenuScreens.<KeypunchContainer, KeypunchScreen>register(
                CEContainers.KEYPUNCH.get(), (container, inv, title) -> new KeypunchScreen(container, title)
        );
        MenuScreens.<LogicDesignContainer, LogicDesignScreen>register(
                CEContainers.LOGIC_DESIGN.get(), (container, inv, title) -> new LogicDesignScreen(container, title)
        );
        MenuScreens.<PanelDesignContainer, PanelDesignScreen>register(
                CEContainers.PANEL_DESIGN.get(), (container, inv, title) -> new PanelDesignScreen(container, title)
        );
    }

    public static ContainerLevelAccess readWorldPos(FriendlyByteBuf buffer) {
        if (buffer == null) {
            return ContainerLevelAccess.NULL;
        }
        BlockPos pos = buffer.readBlockPos();
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return ContainerLevelAccess.NULL;
        }
        return ContainerLevelAccess.create(world, pos);
    }
}
