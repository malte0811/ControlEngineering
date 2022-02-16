package malte0811.controlengineering.blockentity;

import com.google.common.collect.ImmutableSet;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.bus.BusInterfaceBlockEntity;
import malte0811.controlengineering.blockentity.bus.BusRelayBlockEntity;
import malte0811.controlengineering.blockentity.bus.LineAccessBlockEntity;
import malte0811.controlengineering.blockentity.logic.LogicCabinetBlockEntity;
import malte0811.controlengineering.blockentity.logic.LogicWorkbenchBlockEntity;
import malte0811.controlengineering.blockentity.panels.ControlPanelBlockEntity;
import malte0811.controlengineering.blockentity.panels.PanelCNCBlockEntity;
import malte0811.controlengineering.blockentity.panels.PanelDesignerBlockEntity;
import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.blockentity.tape.SequencerBlockEntity;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.logic.LogicCabinetBlock;
import malte0811.controlengineering.blocks.logic.LogicWorkbenchBlock;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.blocks.panels.PanelDesignerBlock;
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

import java.util.function.Predicate;
import java.util.function.Supplier;

public class CEBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITIES, ControlEngineering.MODID
    );

    public static RegistryObject<BlockEntityType<BusRelayBlockEntity>> BUS_RELAY = REGISTER.register(
            "bus_relay", createBEType(BusRelayBlockEntity::new, CEBlocks.BUS_RELAY)
    );

    public static RegistryObject<BlockEntityType<BusInterfaceBlockEntity>> BUS_INTERFACE = REGISTER.register(
            "bus_interface", createBEType(BusInterfaceBlockEntity::new, CEBlocks.BUS_INTERFACE)
    );

    public static RegistryObject<BlockEntityType<LineAccessBlockEntity>> LINE_ACCESS = REGISTER.register(
            "line_access", createBEType(LineAccessBlockEntity::new, CEBlocks.LINE_ACCESS)
    );

    public static MultiblockBEType<ControlPanelBlockEntity, ControlPanelBlockEntity> CONTROL_PANEL = makeMBType(
            "control_panel", ControlPanelBlockEntity::new, CEBlocks.CONTROL_PANEL, PanelBlock::isMaster
    );

    public static MultiblockBEType<PanelCNCBlockEntity, ?> PANEL_CNC = PanelCNCBlockEntity.register(REGISTER);

    public static MultiblockBEType<PanelDesignerBlockEntity, ?> PANEL_DESIGNER = makeMBType(
            "panel_designer", PanelDesignerBlockEntity::new, CEBlocks.PANEL_DESIGNER, PanelDesignerBlock::isMaster
    );

    public static MultiblockBEType<KeypunchBlockEntity, ?> KEYPUNCH = KeypunchBlockEntity.register(REGISTER);

    public static RegistryObject<BlockEntityType<SequencerBlockEntity>> SEQUENCER = REGISTER.register(
            "sequencer", createBEType(SequencerBlockEntity::new, CEBlocks.SEQUENCER)
    );

    public static MultiblockBEType<LogicCabinetBlockEntity, ?> LOGIC_CABINET = makeMBType(
            "logic_cabinet", LogicCabinetBlockEntity::new, CEBlocks.LOGIC_CABINET, LogicCabinetBlock::isMaster
    );

    public static MultiblockBEType<LogicWorkbenchBlockEntity, ?> LOGIC_WORKBENCH = makeMBType(
            "logic_workbench", LogicWorkbenchBlockEntity::new, CEBlocks.LOGIC_WORKBENCH, LogicWorkbenchBlock::isMaster
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

    public static <T extends BlockEntity> MultiblockBEType<T, T> makeMBType(
            String name, BEConstructor<T> make, RegistryObject<? extends Block> valid, Predicate<BlockState> isMaster
    ) {
        return MultiblockBEType.makeType(REGISTER, name, make, valid, isMaster);
    }

    public interface BEConstructor<T extends BlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }
}
