package malte0811.controlengineering.blockentity;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Function3;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.bus.BusInterfaceBlock;
import malte0811.controlengineering.blockentity.bus.BusRelayBlock;
import malte0811.controlengineering.blockentity.bus.LineAccessBlockEntity;
import malte0811.controlengineering.blockentity.logic.LogicCabinetBlockEntity;
import malte0811.controlengineering.blockentity.logic.LogicWorkbenchBlockEntity;
import malte0811.controlengineering.blockentity.panels.ControlPanelBlockEntity;
import malte0811.controlengineering.blockentity.panels.PanelCNCBlockEntity;
import malte0811.controlengineering.blockentity.panels.PanelDesignerBlockEntity;
import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.function.Supplier;

public class CEBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITIES, ControlEngineering.MODID
    );

    public static RegistryObject<BlockEntityType<BusRelayBlock>> BUS_RELAY = REGISTER.register(
            "bus_relay", createBEType(BusRelayBlock::new, CEBlocks.BUS_RELAY)
    );

    public static RegistryObject<BlockEntityType<BusInterfaceBlock>> BUS_INTERFACE = REGISTER.register(
            "bus_interface", createBEType(BusInterfaceBlock::new, CEBlocks.BUS_INTERFACE)
    );

    public static RegistryObject<BlockEntityType<LineAccessBlockEntity>> LINE_ACCESS = REGISTER.register(
            "line_access", createBEType(LineAccessBlockEntity::new, CEBlocks.LINE_ACCESS)
    );

    public static RegistryObject<BlockEntityType<ControlPanelBlockEntity>> CONTROL_PANEL = REGISTER.register(
            "control_panel", createBEType(ControlPanelBlockEntity::new, CEBlocks.CONTROL_PANEL)
    );

    public static MultiblockBEType<PanelCNCBlockEntity> PANEL_CNC = MultiblockBEType.makeType(
            REGISTER, "panel_cnc", PanelCNCBlockEntity::new, CEBlocks.PANEL_CNC, PanelCNCBlock::isMaster
    );

    public static RegistryObject<BlockEntityType<PanelDesignerBlockEntity>> PANEL_DESIGNER = REGISTER.register(
            "panel_designer", createBEType(PanelDesignerBlockEntity::new, CEBlocks.PANEL_DESIGNER)
    );

    public static RegistryObject<BlockEntityType<KeypunchBlockEntity>> KEYPUNCH = REGISTER.register(
            "keypunch", createBEType(KeypunchBlockEntity::new, CEBlocks.KEYPUNCH)
    );

    public static RegistryObject<BlockEntityType<LogicCabinetBlockEntity>> LOGIC_CABINET = REGISTER.register(
            "logic_cabinet", createBEType(LogicCabinetBlockEntity::new, CEBlocks.LOGIC_CABINET)
    );

    public static RegistryObject<BlockEntityType<LogicWorkbenchBlockEntity>> LOGIC_WORKBENCH = REGISTER.register(
            "logic_workbench", createBEType(LogicWorkbenchBlockEntity::new, CEBlocks.LOGIC_WORKBENCH)
    );

    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> createBEType(
            BEConstructor<T> createTE, Supplier<? extends Block> valid
    ) {
        return () -> {
            Mutable<BlockEntityType<T>> type = new MutableObject<>();
            type.setValue(new BlockEntityType<>(
                    (pos, state) -> createTE.create(type.getValue(), pos, state),
                    ImmutableSet.of(valid.get()),
                    null
            ));
            return type.getValue();
        };
    }

    public interface BEConstructor<T extends BlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }
}
