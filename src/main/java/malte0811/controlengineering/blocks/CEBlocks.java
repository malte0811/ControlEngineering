package malte0811.controlengineering.blocks;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.bus.*;
import malte0811.controlengineering.blocks.logic.LogicCabinetBlock;
import malte0811.controlengineering.blocks.logic.LogicWorkbenchBlock;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.blocks.panels.PanelDesignerBlock;
import malte0811.controlengineering.blocks.tape.KeypunchBlock;
import malte0811.controlengineering.blocks.tape.SequencerBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEBlocks {
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(ControlEngineering.MODID);

    public static final DeferredBlock<BusInterfaceBlock> BUS_INTERFACE = REGISTER.register(
            "bus_interface", BusInterfaceBlock::new
    );

    public static final DeferredBlock<BusRelayBlock> BUS_RELAY = REGISTER.register("bus_relay", BusRelayBlock::new);

    public static final DeferredBlock<LineAccessBlock> LINE_ACCESS = REGISTER.register(
            "line_access", LineAccessBlock::new
    );

    public static final DeferredBlock<RSRemapperBlock> RS_REMAPPER = REGISTER.register(
            "rs_remapper", RSRemapperBlock::new
    );

    public static final DeferredBlock<PanelBlock> CONTROL_PANEL = REGISTER.register("control_panel", PanelBlock::new);

    public static final DeferredBlock<PanelCNCBlock> PANEL_CNC = REGISTER.register("panel_cnc", PanelCNCBlock::new);

    public static final DeferredBlock<PanelDesignerBlock> PANEL_DESIGNER = REGISTER.register(
            "panel_designer", PanelDesignerBlock::new
    );

    public static final DeferredBlock<KeypunchBlock> KEYPUNCH = REGISTER.register("keypunch", KeypunchBlock::new);

    public static final DeferredBlock<SequencerBlock> SEQUENCER = REGISTER.register("sequencer", SequencerBlock::new);

    public static final DeferredBlock<LogicCabinetBlock> LOGIC_CABINET = REGISTER.register(
            "logic_cabinet", LogicCabinetBlock::new
    );

    public static final DeferredBlock<LogicWorkbenchBlock> LOGIC_WORKBENCH = REGISTER.register(
            "logic_workbench", LogicWorkbenchBlock::new
    );

    public static final DeferredBlock<ScopeBlock> SCOPE = REGISTER.register(
            "oscilloscope", ScopeBlock::new
    );
}
