package malte0811.controlengineering;

import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.schematic.CellSymbol;
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

        //TODO change?
        addBlock(CEBlocks.LOGIC_BOX, "Logic box");
        addBlock(CEBlocks.LOGIC_WORKBENCH, "Logic workbench");

        add(Constants.PUNCHED_TAPE_BYTES, "Characters: %d");
        add(Constants.EMPTY_TAPE_BYTES, "Length: %d characters");

        addCells();
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
        add(Leafcells.RS_LATCH, "RS-latch");
    }

    private void add(LeafcellType<?> type, String name) {
        add(CellSymbol.getTranslationKey(type), name);
    }
}
