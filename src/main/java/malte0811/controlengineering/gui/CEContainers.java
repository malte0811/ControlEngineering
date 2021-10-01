package malte0811.controlengineering.gui;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.logic.LogicDesignContainer;
import malte0811.controlengineering.gui.panel.PanelDesignContainer;
import malte0811.controlengineering.gui.tape.KeypunchContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.fmllegacy.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CEContainers {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.CONTAINERS, ControlEngineering.MODID
    );

    public static final RegistryObject<MenuType<KeypunchContainer>> KEYPUNCH = REGISTER.register(
            "keypunch", createNoInv(KeypunchContainer::new)
    );

    public static final RegistryObject<MenuType<LogicDesignContainer>> LOGIC_DESIGN = REGISTER.register(
            "logic_design", createNoInv(LogicDesignContainer::new)
    );

    public static final RegistryObject<MenuType<PanelDesignContainer>> PANEL_DESIGN = REGISTER.register(
            "panel_layout", createNoInv(PanelDesignContainer::new)
    );

    private static <T extends AbstractContainerMenu> Supplier<MenuType<T>> createNoInv(BiFunction<Integer, FriendlyByteBuf, T> factory) {
        return create((id, inv, data) -> factory.apply(id, data));
    }

    private static <T extends AbstractContainerMenu> Supplier<MenuType<T>> create(IContainerFactory<T> factory) {
        return () -> new MenuType<>(factory);
    }
}
