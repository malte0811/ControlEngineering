package malte0811.controlengineering.gui;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.bus.RSRemapperBlockEntity;
import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.blockentity.panels.PanelDesignerBlockEntity;
import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.gui.logic.LogicDesignMenu;
import malte0811.controlengineering.gui.logic.LogicDesignMenu.LogicDesignMenuType;
import malte0811.controlengineering.gui.panel.PanelDesignMenu;
import malte0811.controlengineering.gui.remapper.ParallelPortMapperMenu;
import malte0811.controlengineering.gui.remapper.RSRemapperMenu;
import malte0811.controlengineering.gui.scope.ScopeMenu;
import malte0811.controlengineering.gui.tape.KeypunchMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.function.Supplier;

public class CEContainers {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.MENU_TYPES, ControlEngineering.MODID
    );

    public static final ArgMenuType<KeypunchMenu, KeypunchBlockEntity> KEYPUNCH = new ArgMenuType<>(
            REGISTER.register("keypunch", createNoInv(KeypunchMenu::new)), KeypunchMenu::new
    );

    public static final LogicDesignMenuType LOGIC_DESIGN_VIEW = LogicDesignMenu.makeType(
            "logic_design_view", true, REGISTER
    );

    public static final LogicDesignMenuType LOGIC_DESIGN_EDIT = LogicDesignMenu.makeType(
            "logic_design_edit", false, REGISTER
    );

    public static final ArgMenuType<PanelDesignMenu, PanelDesignerBlockEntity> PANEL_DESIGN = new ArgMenuType<>(
            REGISTER.register("panel_layout", createNoInv(PanelDesignMenu::new)), PanelDesignMenu::new
    );

    public static final ArgMenuType<RSRemapperMenu, RSRemapperBlockEntity> RS_REMAPPER = new ArgMenuType<>(
            REGISTER.register("rs_remapper", createNoInv(RSRemapperMenu::new)), RSRemapperMenu::new
    );
    public static final ParallelPortMapperMenu.Type PORT_REMAPPER = new ParallelPortMapperMenu.Type(
            REGISTER.register("port_remapper", createNoInv(ParallelPortMapperMenu::new))
    );

    public static final ArgMenuType<ScopeMenu, ScopeBlockEntity> SCOPE = new ArgMenuType<>(
            REGISTER.register("scope", createNoInv(ScopeMenu::new)), ScopeMenu::new
    );

    private static <T extends AbstractContainerMenu>
    Supplier<MenuType<T>> createNoInv(NoInvMenuFactory<T> factory) {
        return create((type, id, inv) -> factory.create(type, id));
    }

    private static <T extends AbstractContainerMenu>
    Supplier<MenuType<T>> create(ArgMenuFactory<T, Inventory> factory) {
        return () -> {
            Mutable<MenuType<T>> result = new MutableObject<>();
            result.setValue(new MenuType<>(
                    (id, inv) -> factory.create(result.getValue(), id, inv), FeatureFlagSet.of()
            ));
            return result.getValue();
        };
    }

    public record ArgMenuType<M extends AbstractContainerMenu, Arg>(
            RegistryObject<MenuType<M>> type, ArgMenuFactory<M, Arg> construct
    ) {
        public MenuConstructor argConstructor(Arg arg) {
            return (id, $, $2) -> construct.create(type.get(), id, arg);
        }

        public MenuType<M> get() {
            return type.get();
        }
    }

    private interface NoInvMenuFactory<M extends AbstractContainerMenu> {
        M create(MenuType<?> type, int id);
    }

    private interface ArgMenuFactory<M extends AbstractContainerMenu, Arg> {
        M create(MenuType<?> type, int id, Arg extra);
    }
}
