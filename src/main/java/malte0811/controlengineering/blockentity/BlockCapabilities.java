package malte0811.controlengineering.blockentity;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.blockentity.logic.LogicCabinetBlockEntity;
import malte0811.controlengineering.blockentity.panels.PanelCNCBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@EventBusSubscriber
public class BlockCapabilities {
    @SubscribeEvent
    public static void registerBlockCapabilities(RegisterCapabilitiesEvent event) {
        LogicCabinetBlockEntity.registerCapabilities(BlockCapabilities.forType(event, CEBlockEntities.LOGIC_CABINET.master()));
        ScopeBlockEntity.registerCapabilities(BlockCapabilities.forType(event, CEBlockEntities.SCOPE));
        PanelCNCBlockEntity.Dummy.registerCapabilities(BlockCapabilities.forType(event, CEBlockEntities.PANEL_CNC.dummy()));
    }

    private static <BE extends BlockEntity> BECapabilityRegistrar<BE> forType(
            RegisterCapabilitiesEvent ev, Supplier<BlockEntityType<BE>> type
    ) {
        return new BECapabilityRegistrar<>() {
            @Override
            public <C, T> void register(
                    BlockCapability<T, C> capability,
                    ICapabilityProvider<? super BE, C, T> provider
            ) {
                ev.registerBlockEntity(capability, type.get(), provider);
            }
        };
    }

    public interface BECapabilityRegistrar<BE> {
        <C, T> void register(BlockCapability<T, C> capability, ICapabilityProvider<? super BE, C, T> provider);
    }
}