package malte0811.controlengineering.datagen;

import com.google.common.collect.Sets;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.bus.RSRemapperBlockEntity;
import malte0811.controlengineering.blockentity.logic.LogicWorkbenchBlockEntity;
import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.blockentity.tape.SequencerBlockEntity;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.client.manual.CEManual;
import malte0811.controlengineering.client.manual.LeafcellElement;
import malte0811.controlengineering.client.manual.PanelComponentElement;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.gui.logic.LogicDesignScreen;
import malte0811.controlengineering.gui.misc.BusSignalSelector;
import malte0811.controlengineering.gui.misc.ConfirmScreen;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.gui.panel.PanelDesignScreen;
import malte0811.controlengineering.gui.scope.ScopeScreen;
import malte0811.controlengineering.gui.scope.module.AnalogClientModule;
import malte0811.controlengineering.gui.scope.module.ClientModule;
import malte0811.controlengineering.gui.scope.module.DigitalClientModule;
import malte0811.controlengineering.gui.widget.ColorSelector;
import malte0811.controlengineering.items.*;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.impl.InvertingAmplifier;
import malte0811.controlengineering.logic.cells.impl.VoltageDivider;
import malte0811.controlengineering.logic.clock.ClockGenerator;
import malte0811.controlengineering.logic.clock.ClockTypes;
import malte0811.controlengineering.logic.schematic.SchematicChecker;
import malte0811.controlengineering.logic.schematic.symbol.*;
import malte0811.controlengineering.scope.module.ScopeModule;
import malte0811.controlengineering.scope.module.ScopeModules;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LangGenerator extends LanguageProvider {
    private final Set<ResourceLocation> localizedItems = new HashSet<>();

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
        addBlock(CEBlocks.RS_REMAPPER, "Redstone Wire Remapper");

        addItem(CEItems.EMPTY_TAPE, "Empty Tape");
        addItem(CEItems.PUNCHED_TAPE, "Punched Tape");
        addItem(CEItems.PANEL_TOP, "Panel Top");
        addItem(CEItems.PCB_STACK, "Logic Circuit Boards");
        addItem(CEItems.BUS_WIRE_COIL, "Bus Wire Coil");
        addItem(CEItems.SCHEMATIC, "Logic Circuit Schematic");
        addItem(CEItems.KEY, "Key");
        addItem(CEItems.LOCK, "Lock with key");

        addBlock(CEBlocks.LOGIC_CABINET, "Logic Cabinet");
        addBlock(CEBlocks.LOGIC_WORKBENCH, "Logic Workbench");
        addClock(ClockTypes.ALWAYS_ON, "Clock Generator: Free running");
        addClock(ClockTypes.RISING_EDGE, "Clock Generator: Rising edge");
        addClock(ClockTypes.WHILE_RS_ON, "Clock Generator: State triggered");

        add(PunchedTapeItem.PUNCHED_TAPE_BYTES, "Characters: %d");
        add(EmptyTapeItem.EMPTY_TAPE_BYTES, "Length: %d characters");

        addBlock(CEBlocks.SCOPE, "Oscilloscope");
        addScopeModule(ScopeModules.ANALOG, "Dual Trace Amplifier Module");
        addScopeModule(ScopeModules.DIGITAL, "Logic Analyzer Module");

        add("itemGroup." + ControlEngineering.MODID, "Control Engineering");

        addCells();
        addPanelComponents();
        addGuiStrings();
        addManualStrings();

        assertAllLocalized(localizedItems, CEItems.REGISTER);
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
        add(Leafcells.ANALOG_MUX, "Analog Multiplexer");
        add(Leafcells.DIGITAL_MUX, "Digital Multiplexer");
        add(Leafcells.DIVIDER, "Voltage Divider");
        add(Leafcells.ANALOG_ADDER, "Analog Adder");
        add(Leafcells.INVERTING_AMPLIFIER, "Inverting Amplifier");

        add(IOSymbol.ANALOG_INPUT_KEY, "Analog input pin");
        add(IOSymbol.DIGITAL_INPUT_KEY, "Digitized input pin");
        add(IOSymbol.OUTPUT_KEY, "Output pin");
        add(ConstantSymbol.NAME, "Constant");
        add(TextSymbol.NAME_KEY, "Text");
        add(Leafcells.CONFIG_SWITCH, "Configuration switch");
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
        add(PanelComponents.KEY_SWITCH, "Key Switch");
    }

    private void addGuiStrings() {
        add(BusSignalSelector.COLOR_KEY, "Signal color");
        add(BusSignalSelector.BUS_LINE_INDEX_KEY, "Bus line %d");
        add(DataProviderScreen.DONE_KEY, "Done");

        add(IOSymbol.SIGNAL_KEY, "%d signal on line %d");
        add(ConstantSymbol.INPUT_KEY, "Signal strength: %d");

        add(LogicDesignScreen.COMPONENTS_KEY, "Add");
        add(LogicDesignScreen.COMPONENTS_TOOLTIP, "Add new components");
        add(LogicDesignScreen.CLEAR_ALL_KEY, "Clear");
        add(LogicDesignScreen.CLEAR_ALL_TOOLTIP, "Remove all components and wires");
        add(LogicDesignScreen.CLEAR_ALL_MESSAGE, "Clear entire schematic?");
        add(LogicDesignScreen.SET_NAME_KEY, "Name");
        add(LogicDesignScreen.SET_NAME_TOOLTIP, "Set schematic name");
        add(LogicDesignScreen.SET_NAME_MESSAGE, "Enter new schematic name");
        add(LogicDesignScreen.DRC_INFO_KEY, "Highlight floating input pins");
        add(LogicDesignScreen.ANALOG_PIN_KEY, "Analog pin: %d");
        add(LogicDesignScreen.DIGITAL_PIN_KEY, "Digital pin: %d");

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

        add(SequencerBlockEntity.AUTORESET_KEY, "Resets automatically");
        add(SequencerBlockEntity.MANUAL_RESET_KEY, "Requires manual reset");
        add(SequencerBlockEntity.ANALOG_KEY, "Analog mode");
        add(SequencerBlockEntity.COMPACT_KEY, "Compact mode");

        add(RSRemapperBlockEntity.COLORED_KEY, "Colored");
        add(RSRemapperBlockEntity.GRAY_KEY, "Gray");

        add(PCBStackItem.FOR_USE_IN_KEY, "For use in a %s");

        add(ConfirmScreen.CANCEL_KEY, "Cancel");
        add(ConfirmScreen.OK_KEY, "Ok");

        add(SchematicItem.EMPTY_SCHEMATIC, "Empty schematic");

        add(VoltageDivider.RESISTANCE_KEY, "Lower resistance: %d");

        add(InvertingAmplifier.AMPLIFY_BY, "Amplify by %dx");
        add(InvertingAmplifier.ATTENUATE_BY, "Attenuate by %dx");

        add(ConfigSwitchSymbol.OFF_KEY, "Low output");
        add(ConfigSwitchSymbol.ON_KEY, "High output");

        add(AnalogClientModule.TRIGGER_POLARITY_TOOLTIP, "Trigger slope polarity");
        add(AnalogClientModule.PER_DIV_TOOLTIP, "Signal per vertical division");
        add(AnalogClientModule.TRIGGER_LEVEL_TOOLTIP, "Trigger level");
        add(AnalogClientModule.BNC_OPEN, "Signal input (not connected)");
        add(AnalogClientModule.BNC_CONNECTED, "Signal input (%s on line %d)");
        add(DigitalClientModule.TRIGGER_HIGH, "Trigger when high");
        add(DigitalClientModule.TRIGGER_LOW, "Trigger when low");
        add(DigitalClientModule.TRIGGER_IGNORE, "Ignore for triggering");
        add(DigitalClientModule.INPUT_OPEN, "Logic input (not connected)");
        add(DigitalClientModule.INPUT_CONNECTED, "Logic input (line %d)");
        add(ClientModule.TRIGGER_SOURCE_USED, "Used as trigger source");
        add(ClientModule.TRIGGER_SOURCE_UNUSED, "Not used as trigger source");
        add(ClientModule.CHANNEL_SHOWN, "Showing channel");
        add(ClientModule.CHANNEL_HIDDEN, "Hiding channel");
        add(ClientModule.MODULE_ACTIVE, "Module enabled");
        add(ClientModule.MODULE_INACTIVE, "Module disabled");
        add(ClientModule.VERT_OFFSET_TOOLTIP, "Vertical offset in pixels");
        add(ScopeScreen.TICKS_PER_DIV_KEY, "Ticks per horizontal division");
    }

    private void addManualStrings() {
        addManualSection("main", "Control Engineering");
        addManualSection("logic", "Logic Cabinets");
        addManualSection("panels", "Control Panels");

        add(CEManual.EXAMPLE_KEY, "Example");
        add(CEManual.OPTIONS_KEY, "Options");
        add(CEManual.NAME_KEY, "Name");
        add(CEManual.CODEC_NAMES.get(MyCodecs.HEX_COLOR), "color");
        add(CEManual.CODEC_NAMES.get(MyCodecs.STRING), "text");
        add(CEManual.CODEC_NAMES.get(MyCodecs.INTEGER), "integer");

        add(PanelComponentElement.INGREDIENTS_KEY, "Ingredients");
        add(LeafcellElement.COST_KEY, "Cost");
    }

    private void add(LeafcellType<?, ?> type, String name) {
        add(CellSymbol.getTranslationKey(type), name);
    }

    private void add(PanelComponentType<?, ?> type, String name) {
        add(type.getTranslationKey(), name);
    }

    private void addClock(ClockGenerator<?> type, String name) {
        addItem(CEItems.CLOCK_GENERATORS.get(type.getRegistryName()), name);
    }

    private void addScopeModule(ScopeModule<?> type, String name) {
        addItem(CEItems.SCOPE_MODULES.get(type.getRegistryName()), name);
    }

    private void addManualSection(String id, String name) {
        add("manual." + ControlEngineering.MODID + '.' + id, name);
    }

    @Override
    public void addBlock(@Nonnull Supplier<? extends Block> key, @Nonnull String name) {
        super.addBlock(key, name);
        localizedItems.add(Registry.BLOCK.getKey(key.get()));
    }

    @Override
    public void addItem(@Nonnull Supplier<? extends Item> key, @Nonnull String name) {
        super.addItem(key, name);
        localizedItems.add(Registry.ITEM.getKey(key.get()));
    }

    private void assertAllLocalized(Set<ResourceLocation> localized, DeferredRegister<?> register) {
        var allRLs = register.getEntries().stream()
                .map(RegistryObject::getId)
                .collect(Collectors.toSet());
        var unregistered = Sets.difference(allRLs, localized);
        if (!unregistered.isEmpty()) {
            var error = unregistered.stream()
                    .map(ResourceLocation::toString)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Unregistered: " + error);
        }
    }
}
