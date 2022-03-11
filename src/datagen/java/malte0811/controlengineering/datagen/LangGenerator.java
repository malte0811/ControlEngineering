package malte0811.controlengineering.datagen;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.logic.LogicWorkbenchBlockEntity;
import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.blockentity.tape.SequencerBlockEntity;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.client.manual.CEManual;
import malte0811.controlengineering.client.manual.PanelComponentElement;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.gui.logic.LogicDesignScreen;
import malte0811.controlengineering.gui.misc.BusSignalSelector;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.gui.panel.PanelDesignScreen;
import malte0811.controlengineering.gui.widget.ColorSelector;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.ControlPanelItem;
import malte0811.controlengineering.items.EmptyTapeItem;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.clock.ClockGenerator;
import malte0811.controlengineering.logic.clock.ClockTypes;
import malte0811.controlengineering.logic.schematic.SchematicChecker;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.logic.schematic.symbol.ConstantSymbol;
import malte0811.controlengineering.logic.schematic.symbol.IOSymbol;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class LangGenerator extends LanguageProvider {
    public LangGenerator(DataGenerator gen) {
        super(gen, ControlEngineering.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addBlock(CEBlocks.CONTROL_PANEL, "Control Panel");
        addBlock(CEBlocks.BUS_INTERFACE, "Bus Interface");
        addBlock(CEBlocks.BUS_RELAY, "Bus Relay");
        addBlock(CEBlocks.LINE_ACCESS, "Line Access");
        addBlock(CEBlocks.PANEL_CNC, "Control Panel CNC");
        addBlock(CEBlocks.KEYPUNCH, "Keyboard Perforator");
        addBlock(CEBlocks.SEQUENCER, "Signal Sequencer");
        addBlock(CEBlocks.PANEL_DESIGNER, "Panel Designer");

        addItem(CEItems.EMPTY_TAPE, "Empty Tape");
        addItem(CEItems.PUNCHED_TAPE, "Punched Tape");
        addItem(CEItems.PANEL_TOP, "Panel Top");
        //TODO?
        addItem(CEItems.PCB_STACK, "Logic Circuit Boards");
        addItem(CEItems.BUS_WIRE_COIL, "Bus Wire Coil");

        addBlock(CEBlocks.LOGIC_CABINET, "Logic Cabinet");
        addBlock(CEBlocks.LOGIC_WORKBENCH, "Logic Workbench");
        addClock(ClockTypes.ALWAYS_ON, "Clock Generator: Free running");
        addClock(ClockTypes.RISING_EDGE, "Clock Generator: Rising edge");
        addClock(ClockTypes.WHILE_RS_ON, "Clock Generator: State triggered");

        add(PunchedTapeItem.PUNCHED_TAPE_BYTES, "Characters: %d");
        add(EmptyTapeItem.EMPTY_TAPE_BYTES, "Length: %d characters");

        add("itemGroup." + ControlEngineering.MODID, "Control Engineering");

        addCells();
        addPanelComponents();
        addGuiStrings();
        addManualStrings();
    }

    private void addCells() {
        add(Leafcells.AND2, "2-input AND");
        add(Leafcells.AND3, "3-input AND");
        add(Leafcells.OR2, "2-input OR");
        add(Leafcells.OR3, "3-input OR");

        add(Leafcells.NAND2, "2-input NAND");
        add(Leafcells.NAND3, "3-input NAND");
        add(Leafcells.NOR2, "2-input NOR");
        add(Leafcells.NOR3, "3-input NOR");

        add(Leafcells.XOR2, "2-input XOR");
        add(Leafcells.XOR3, "3-input XOR");

        add(Leafcells.NOT, "Inverter");
        add(Leafcells.RS_LATCH, "RS latch");
        add(Leafcells.SCHMITT_TRIGGER, "Schmitt Trigger");

        add(Leafcells.D_LATCH, "D Flip-Flop");
        add(Leafcells.DELAY_LINE, "Delay Line");

        add(Leafcells.DIGITIZER, "Signal Digitizer");
        add(Leafcells.COMPARATOR, "Comparator");
    }

    private void addPanelComponents() {
        add(PanelComponents.BUTTON, "Button");
        add(PanelComponents.INDICATOR, "Indicator");
        add(PanelComponents.LABEL, "Label");
        add(PanelComponents.TOGGLE_SWITCH, "Toggle Switch");
        add(PanelComponents.COVERED_SWITCH, "Covered Switch");
        add(PanelComponents.TIMED_BUTTON, "Timed Button");
        add(PanelComponents.PANEL_METER, "Panel Meter");
        add(PanelComponents.VARIAC, "Variac");
        add(PanelComponents.SLIDER_VERT, "Slider");
        add(PanelComponents.SLIDER_HOR, "Slider");
    }

    private void addGuiStrings() {
        add(BusSignalSelector.COLOR_KEY, "Signal color");
        add(BusSignalSelector.BUS_LINE_INDEX_KEY, "Bus line %d");
        add(DataProviderScreen.DONE_KEY, "Done");

        add(IOSymbol.INPUT_KEY, "Input pin");
        add(IOSymbol.OUTPUT_KEY, "Output pin");
        add(IOSymbol.SIGNAL_KEY, "%d signal on line %d");

        add(ConstantSymbol.NAME, "Constant");
        add(ConstantSymbol.INPUT_KEY, "Signal strength: %d");

        add(LogicDesignScreen.COMPONENTS_KEY, "Components");
        add(LogicDesignScreen.ENABLE_DRC_KEY, "Enable error checking");
        add(LogicDesignScreen.DISABLE_DRC_KEY, "Disable error checking");
        add(LogicDesignScreen.PIN_KEY, "Pin: %d");

        add(SchematicChecker.ANALOG_DIGITAL_MIX, "Net would connect an analog source to digital sinks");
        add(SchematicChecker.CYCLE, "Net would form a cycle");
        add(SchematicChecker.MULTIPLE_SOURCES, "Net would contain multiple source pins");
        add(SchematicChecker.WIRE_OUTSIDE_BOUNDARY, "Wire segment would be outside of schematic boundary");
        add(SchematicChecker.SYMBOL_OUTSIDE_BOUNDARY, "Symbol would be outside of schematic boundary");
        add(SchematicChecker.SYMBOL_INTERSECTION, "Symbol would intersect with other symbols");

        add(ColorSelector.RED, "Red: %d");
        add(ColorSelector.GREEN, "Green: %d");
        add(ColorSelector.BLUE, "Blue: %d");

        add(LogicWorkbenchBlockEntity.TUBES_EMPTY_KEY, "Vacuum tube storage: Empty");
        add(LogicWorkbenchBlockEntity.WIRES_EMPTY_KEY, "Wire storage: Empty");
        add(
                LogicWorkbenchBlockEntity.MORE_BOARDS_THAN_MAX,
                "Circuit would need %d circuit boards, but only %d fit in the logic cabinet"
        );
        add(LogicWorkbenchBlockEntity.TOO_FEW_BOARDS_HELD, "%d circuit boards are required");
        add(LogicWorkbenchBlockEntity.TOO_FEW_WIRES, "%d wires are required");
        add(LogicWorkbenchBlockEntity.TOO_FEW_TUBES, "%d vacuum tubes are required");

        add(PanelDesignScreen.REQUIRED_VS_AVAILABLE_TAPE, "%d characters required / %d available");

        add(KeypunchBlockEntity.LOOPBACK_KEY, "Loopback");
        add(KeypunchBlockEntity.REMOTE_KEY, "Remote transmission");

        add(ControlPanelItem.getKey(ControlPanelItem.BACK_HEIGHT_OPTION), "Back height");
        add(ControlPanelItem.getKey(ControlPanelItem.FRONT_HEIGHT_OPTION), "Front height");
        add(PanelComponentElement.INGREDIENTS_KEY, "Ingredients");

        add(SequencerBlockEntity.AUTORESET_KEY, "Resets automatically");
        add(SequencerBlockEntity.MANUAL_RESET_KEY, "Requires manual reset");
        add(SequencerBlockEntity.ANALOG_KEY, "Analog mode");
        add(SequencerBlockEntity.COMPACT_KEY, "Compact mode");
    }

    private void addManualStrings() {
        addManualSection("main", "Control Engineering");
        addManualSection("logic", "Logic Cabinets");
        addManualSection("panels", "Control Panels");

        add(CEManual.EXAMPLE_KEY, "Example");
        add(CEManual.OPTIONS_KEY, "Options");
        add(CEManual.NAME_KEY, "Name");
        add(CEManual.CODEC_NAMES.get(MyCodecs.HEX_INTEGER), "color");
        add(CEManual.CODEC_NAMES.get(MyCodecs.STRING), "text");
        add(CEManual.CODEC_NAMES.get(MyCodecs.INTEGER), "integer");
    }

    private void add(LeafcellType<?> type, String name) {
        add(CellSymbol.getTranslationKey(type), name);
    }

    private void add(PanelComponentType<?, ?> type, String name) {
        add(type.getTranslationKey(), name);
    }

    private void addClock(ClockGenerator<?> type, String name) {
        addItem(CEItems.CLOCK_GENERATORS.get(type.getRegistryName()), name);
    }

    private void addManualSection(String id, String name) {
        add("manual." + ControlEngineering.MODID + '.' + id, name);
    }
}
