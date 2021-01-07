package malte0811.controlengineering;

import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.items.CEItems;
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

        add(Constants.PUNCHED_TAPE_BYTES, "Characters: %d");
        add(Constants.EMPTY_TAPE_BYTES, "Length: %d characters");
    }
}
