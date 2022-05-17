package malte0811.controlengineering.gui;

import malte0811.controlengineering.gui.logic.LogicDesignMenu;
import malte0811.controlengineering.gui.logic.LogicDesignScreen;
import malte0811.controlengineering.gui.panel.PanelDesignMenu;
import malte0811.controlengineering.gui.panel.PanelDesignScreen;
import malte0811.controlengineering.gui.remapper.AbstractRemapperMenu;
import malte0811.controlengineering.gui.remapper.AbstractRemapperScreen;
import malte0811.controlengineering.gui.remapper.RSRemapperScreen;
import malte0811.controlengineering.gui.tape.KeypunchMenu;
import malte0811.controlengineering.gui.tape.KeypunchScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class ContainerScreenManager {
    // IDEA considers the type arguments to be redundant, but the compiler disagrees, and that's the thing that
    // actually *needs* to like my code, so it wins
    @SuppressWarnings("RedundantTypeArguments")
    public static void registerScreens() {
        MenuScreens.<KeypunchMenu, KeypunchScreen>register(
                CEContainers.KEYPUNCH.get(), (container, inv, title) -> new KeypunchScreen(container, title)
        );
        MenuScreens.<LogicDesignMenu, LogicDesignScreen>register(
                CEContainers.LOGIC_DESIGN_EDIT.get(), (container, inv, title) -> new LogicDesignScreen(container, title)
        );
        MenuScreens.<LogicDesignMenu, LogicDesignScreen>register(
                CEContainers.LOGIC_DESIGN_VIEW.get(), (container, inv, title) -> new LogicDesignScreen(container, title)
        );
        MenuScreens.<PanelDesignMenu, PanelDesignScreen>register(
                CEContainers.PANEL_DESIGN.get(), (container, inv, title) -> new PanelDesignScreen(container, title)
        );
        MenuScreens.<AbstractRemapperMenu, AbstractRemapperScreen>register(
                CEContainers.RS_REMAPPER.get(), (container, inv, title) -> new RSRemapperScreen(container)
        );
    }

    public static Predicate<Player> isValidFor(BlockEntity menuBE) {
        return p -> !menuBE.isRemoved() && p.distanceToSqr(Vec3.atCenterOf(menuBE.getBlockPos())) <= 64.0D;
    }
}
