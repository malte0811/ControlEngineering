package malte0811.controlengineering.gui;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.logic.LogicDesignMenu;
import malte0811.controlengineering.gui.logic.LogicDesignScreen;
import malte0811.controlengineering.gui.panel.PanelDesignMenu;
import malte0811.controlengineering.gui.panel.PanelDesignScreen;
import malte0811.controlengineering.gui.remapper.AbstractRemapperMenu;
import malte0811.controlengineering.gui.remapper.AbstractRemapperScreen;
import malte0811.controlengineering.gui.remapper.ParallelPortMapperScreen;
import malte0811.controlengineering.gui.remapper.RSRemapperScreen;
import malte0811.controlengineering.gui.scope.ScopeMenu;
import malte0811.controlengineering.gui.scope.ScopeScreen;
import malte0811.controlengineering.gui.tape.KeypunchMenu;
import malte0811.controlengineering.gui.tape.KeypunchScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.validation.PathAllowList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ContainerScreenManager {
    // IDEA considers the type arguments to be redundant, but the compiler disagrees, and that's the thing that
    // actually *needs* to like my code, so it wins
    // TODO check if still necessary
    @SuppressWarnings("RedundantTypeArguments")
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent ev) {
        ev.<KeypunchMenu, KeypunchScreen>register(
                CEContainers.KEYPUNCH.get(), (container, inv, title) -> new KeypunchScreen(container, title)
        );
        ev.<LogicDesignMenu, LogicDesignScreen>register(
                CEContainers.LOGIC_DESIGN_EDIT.get(), (container, inv, title) -> new LogicDesignScreen(container, title)
        );
        ev.<LogicDesignMenu, LogicDesignScreen>register(
                CEContainers.LOGIC_DESIGN_VIEW.get(), (container, inv, title) -> new LogicDesignScreen(container, title)
        );
        ev.<PanelDesignMenu, PanelDesignScreen>register(
                CEContainers.PANEL_DESIGN.get(), (container, inv, title) -> new PanelDesignScreen(container, title)
        );
        ev.<AbstractRemapperMenu, AbstractRemapperScreen>register(
                CEContainers.RS_REMAPPER.get(), (container, inv, title) -> new RSRemapperScreen(container)
        );
        ev.<AbstractRemapperMenu, AbstractRemapperScreen>register(
                CEContainers.PORT_REMAPPER.get(), (container, inv, title) -> new ParallelPortMapperScreen(container)
        );
        ev.<ScopeMenu, ScopeScreen>register(
                CEContainers.SCOPE.get(), (container, inv, title) -> new ScopeScreen(container)
        );
    }
}
