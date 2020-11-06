package malte0811.controlengineering.gui;

import malte0811.controlengineering.ControlEngineering;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CEContainers {
    public static final DeferredRegister<ContainerType<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.CONTAINERS, ControlEngineering.MODID
    );

    public static final RegistryObject<ContainerType<TeletypeContainer>> TELETYPE = REGISTER.register(
            "teletype", createNoInv(TeletypeContainer::new)
    );

    private static <T extends Container> Supplier<ContainerType<T>> createNoInv(BiFunction<Integer, PacketBuffer, T> factory) {
        return create((id, inv, data) -> factory.apply(id, data));
    }

    private static <T extends Container> Supplier<ContainerType<T>> create(IContainerFactory<T> factory) {
        return () -> new ContainerType<>(factory);
    }
}
