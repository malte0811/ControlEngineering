package malte0811.controlengineering.blocks;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.bus.BusInterfaceBlock;
import malte0811.controlengineering.blocks.bus.BusRelayBlock;
import malte0811.controlengineering.blocks.bus.LineAccessBlock;
import malte0811.controlengineering.blocks.logic.LogicCabinetBlock;
import malte0811.controlengineering.blocks.logic.LogicWorkbenchBlock;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.blocks.panels.PanelDesignerBlock;
import malte0811.controlengineering.blocks.tape.KeypunchBlock;
import malte0811.controlengineering.blocks.tape.SequencerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CEBlocks {
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(
            ForgeRegistries.BLOCKS,
            ControlEngineering.MODID
    );

    public static final RegistryObject<BusInterfaceBlock> BUS_INTERFACE = REGISTER.register(
            "bus_interface", BusInterfaceBlock::new
    );

    public static final RegistryObject<BusRelayBlock> BUS_RELAY = REGISTER.register("bus_relay", BusRelayBlock::new);

    public static final RegistryObject<LineAccessBlock> LINE_ACCESS = REGISTER.register(
            "line_access", LineAccessBlock::new
    );

    public static final RegistryObject<PanelBlock> CONTROL_PANEL = REGISTER.register("control_panel", PanelBlock::new);

    public static final RegistryObject<PanelCNCBlock> PANEL_CNC = REGISTER.register("panel_cnc", PanelCNCBlock::new);

    public static final RegistryObject<PanelDesignerBlock> PANEL_DESIGNER = REGISTER.register(
            "panel_designer", PanelDesignerBlock::new
    );

    public static final RegistryObject<KeypunchBlock> KEYPUNCH = REGISTER.register("keypunch", KeypunchBlock::new);

    public static final RegistryObject<SequencerBlock> SEQUENCER = REGISTER.register("sequencer", SequencerBlock::new);

    public static final RegistryObject<LogicCabinetBlock> LOGIC_CABINET = REGISTER.register(
            "logic_cabinet",
            LogicCabinetBlock::new
    );

    public static final RegistryObject<LogicWorkbenchBlock> LOGIC_WORKBENCH = REGISTER.register(
            "logic_workbench", LogicWorkbenchBlock::new
    );
}
