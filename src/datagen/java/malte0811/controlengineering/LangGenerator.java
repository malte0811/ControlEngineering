package malte0811.controlengineering;

import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.gui.logic.LogicDesignScreen;
import malte0811.controlengineering.gui.widgets.BusSignalSelector;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.clock.ClockGenerator;
import malte0811.controlengineering.logic.clock.ClockTypes;
import malte0811.controlengineering.logic.schematic.SchematicChecker;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.logic.schematic.symbol.ConstantSymbol;
import malte0811.controlengineering.logic.schematic.symbol.IOSymbol;
import malte0811.controlengineering.util.Constants;
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
        //TODO change?
        addBlock(CEBlocks.TELETYPE, "Teletype");

        addItem(CEItems.EMPTY_TAPE, "Empty Tape");
        addItem(CEItems.PUNCHED_TAPE, "Punched Tape");
        addItem(CEItems.PANEL_TOP, "Panel Top");
        //TODO?
        addItem(CEItems.PCB_STACK, "Logic Circuit Boards");

        addBlock(CEBlocks.LOGIC_CABINET, "Logic Cabinet");
        addBlock(CEBlocks.LOGIC_WORKBENCH, "Logic Workbench");
        addClock(ClockTypes.ALWAYS_ON, "Clock Generator: Free running");
        addClock(ClockTypes.RISING_EDGE, "Clock Generator: Rising edge");
        addClock(ClockTypes.WHILE_RS_ON, "Clock Generator: State triggered");

        add(Constants.PUNCHED_TAPE_BYTES, "Characters: %d");
        add(Constants.EMPTY_TAPE_BYTES, "Length: %d characters");

        addCells();
        addGuiStrings();
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
    }

    private void addGuiStrings() {
        add(BusSignalSelector.COLOR_KEY, "Signal color");
        add(BusSignalSelector.BUS_LINE_INDEX_KEY, "Bus line %d");
        add(BusSignalSelector.DONE_KEY, "Done");

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
    }

    private void add(LeafcellType<?> type, String name) {
        add(CellSymbol.getTranslationKey(type), name);
    }

    private void addClock(ClockGenerator<?> type, String name) {
        addItem(CEItems.CLOCK_GENERATORS.get(type.getRegistryName()), name);
    }
}
