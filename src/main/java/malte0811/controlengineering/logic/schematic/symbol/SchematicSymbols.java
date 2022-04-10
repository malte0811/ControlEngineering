package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.cells.impl.*;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static malte0811.controlengineering.logic.cells.PinDirection.*;
import static malte0811.controlengineering.logic.schematic.symbol.SymbolPin.*;

public class SchematicSymbols {
    public static final TypedRegistry<SchematicSymbol<?>> REGISTRY = new TypedRegistry<>();
    public static final ResourceLocation SYMBOLS_SHEET = new ResourceLocation(
            ControlEngineering.MODID, "textures/gui/schematic_symbols.png"
    );
    public static final IOSymbol INPUT_PIN_ANALOG = new IOSymbol(true, false);
    public static final IOSymbol INPUT_PIN_DIGITAL = new IOSymbol(true, true);
    public static final IOSymbol OUTPUT_PIN = new IOSymbol(false, false);
    public static final ConstantSymbol CONSTANT = new ConstantSymbol();
    public static final CellSymbol<Unit> NOT;
    public static final CellSymbol<Unit> AND2;
    public static final CellSymbol<Unit> AND3;
    public static final CellSymbol<Unit> OR2;
    public static final CellSymbol<Unit> OR3;
    public static final CellSymbol<Unit> NAND2;
    public static final CellSymbol<Unit> NAND3;
    public static final CellSymbol<Unit> NOR2;
    public static final CellSymbol<Unit> NOR3;
    public static final CellSymbol<Unit> XOR2;
    public static final CellSymbol<Unit> XOR3;
    public static final CellSymbol<Unit> RS_LATCH;
    public static final CellSymbol<Unit> SCHMITT_TRIGGER;
    public static final CellSymbol<Unit> DELAY_LINE;
    public static final CellSymbol<Unit> D_LATCH;
    public static final CellSymbol<Unit> DIGITIZER;
    public static final CellSymbol<Unit> COMPARATOR;
    public static final CellSymbol<Unit> ANALOG_MUX;
    public static final CellSymbol<Unit> DIGITAL_MUX;
    public static final CellSymbol<Integer> VOLTAGE_DIVIDER;
    public static final TextSymbol TEXT = new TextSymbol();

    static {
        List<SymbolPin> twoInputPins = ImmutableList.of(digitalIn(0, 1, "in1"), digitalIn(0, 5, "in2"));
        List<SymbolPin> threeInputPinsFlush = ImmutableList.of(
                digitalIn(0, 1, "in1"),
                digitalIn(0, 3, "in2"),
                digitalIn(0, 5, "in3")
        );
        List<SymbolPin> threeInputPinsShift = ImmutableList.of(
                digitalIn(0, 1, "in1"),
                digitalIn(1, 3, "in2"),
                digitalIn(0, 5, "in3")
        );

        AND2 = registerSimpleCell(Leafcells.AND2, 9, twoInputPins);
        AND3 = registerSimpleCell(Leafcells.AND3, 9, threeInputPinsFlush);

        OR2 = registerSimpleCell(Leafcells.OR2, 11, twoInputPins);
        OR3 = registerSimpleCell(Leafcells.OR3, 11, threeInputPinsShift);

        NAND2 = registerSimpleCell(Leafcells.NAND2, 12, twoInputPins);
        NAND3 = registerSimpleCell(Leafcells.NAND3, 12, threeInputPinsFlush);

        NOR2 = registerSimpleCell(Leafcells.NOR2, 13, twoInputPins);
        NOR3 = registerSimpleCell(Leafcells.NOR3, 13, threeInputPinsShift);

        XOR2 = registerSimpleCell(Leafcells.XOR2, 13, twoInputPins);
        XOR3 = registerSimpleCell(Leafcells.XOR3, 13, threeInputPinsShift);

        NOT = registerSimpleCell(
                Leafcells.NOT, 13, ImmutableList.of(digitalIn(0, 3, LeafcellType.DEFAULT_IN_NAME))
        );
        RS_LATCH = registerCell(
                Leafcells.RS_LATCH, 13, ImmutableList.of(
                        digitalIn(0, 1, RSLatch.SET),
                        digitalIn(0, 5, RSLatch.RESET),
                        new SymbolPin(12, 1, SignalType.DIGITAL, DELAYED_OUTPUT, RSLatch.Q),
                        new SymbolPin(12, 5, SignalType.DIGITAL, DELAYED_OUTPUT, RSLatch.NOT_Q)
                )
        );

        SCHMITT_TRIGGER = registerSimpleCell(
                Leafcells.SCHMITT_TRIGGER, 13, ImmutableList.of(
                        analogIn(0, 1, SchmittTrigger.HIGH_PIN),
                        analogIn(0, 3, LeafcellType.DEFAULT_IN_NAME),
                        analogIn(0, 5, SchmittTrigger.LOW_PIN)
                )
        );
        DIGITIZER = registerSimpleCell(
                Leafcells.DIGITIZER, 13,
                ImmutableList.of(analogIn(0, 3, LeafcellType.DEFAULT_IN_NAME))
        );
        COMPARATOR = registerSimpleCell(
                Leafcells.COMPARATOR, 13, ImmutableList.of(
                        analogIn(0, 1, Comparator.NEGATIVE), analogIn(0, 5, Comparator.POSITIVE)
                )
        );

        D_LATCH = delayCell(Leafcells.D_LATCH, 10, SignalType.DIGITAL);
        DELAY_LINE = delayCell(Leafcells.DELAY_LINE, 13, SignalType.ANALOG);
        ANALOG_MUX = registerMUX(Leafcells.ANALOG_MUX, SignalType.ANALOG);
        DIGITAL_MUX = registerMUX(Leafcells.DIGITAL_MUX, SignalType.DIGITAL);
        VOLTAGE_DIVIDER = registerCell(Leafcells.DIVIDER, 8, 11, List.of(
                analogIn(0, 0, VoltageDivider.INPUT_TOP),
                analogIn(0, 10, VoltageDivider.INPUT_BOTTOM),
                analogOut(7, 5, VoltageDivider.DEFAULT_OUT_NAME)
        ));

        REGISTRY.register(new ResourceLocation(ControlEngineering.MODID, "input_pin"), INPUT_PIN_ANALOG);
        REGISTRY.register(new ResourceLocation(ControlEngineering.MODID, "input_pin_digitized"), INPUT_PIN_DIGITAL);
        REGISTRY.register(new ResourceLocation(ControlEngineering.MODID, "output_pin"), OUTPUT_PIN);
        REGISTRY.register(new ResourceLocation(ControlEngineering.MODID, "constant"), CONSTANT);
        REGISTRY.register(new ResourceLocation(ControlEngineering.MODID, "text"), TEXT);
    }

    private static CellSymbol<Unit> delayCell(LeafcellType<?, Unit> cell, int uSize, SignalType type) {
        List<SymbolPin> pins = ImmutableList.of(
                new SymbolPin(0, 3, type, PinDirection.INPUT, LeafcellType.DEFAULT_IN_NAME),
                new SymbolPin(uSize - 1, 3, type, DELAYED_OUTPUT, LeafcellType.DEFAULT_OUT_NAME)
        );
        return registerCell(cell, uSize, pins);
    }

    private static CellSymbol<Unit> registerMUX(LeafcellType<?, Unit> cell, SignalType type) {
        return registerCell(cell, 7, 8, List.of(
                new SymbolPin(0, 1, type, INPUT, Multiplexer.INPUT_0),
                new SymbolPin(0, 5, type, INPUT, Multiplexer.INPUT_1),
                new SymbolPin(new Vec2i(4, 7), SignalType.DIGITAL, INPUT, Multiplexer.SELECT, true),
                new SymbolPin(6, 3, type, OUTPUT, Multiplexer.OUTPUT)
        ));
    }

    private static <C> CellSymbol<C> registerSimpleCell(
            LeafcellType<?, C> cell, int uSize, List<SymbolPin> inputPins
    ) {
        List<SymbolPin> allPins = new ArrayList<>(inputPins);
        allPins.add(digitalOut(uSize - 1, 3, LeafcellType.DEFAULT_OUT_NAME));
        return registerCell(cell, uSize, allPins);
    }

    private static <C> CellSymbol<C> registerCell(LeafcellType<?, C> cell, int uSize, List<SymbolPin> pins) {
        return registerCell(cell, uSize, 7, pins);
    }

    private static <C> CellSymbol<C> registerCell(LeafcellType<?, C> cell, int uSize, int vSize, List<SymbolPin> pins) {
        return REGISTRY.register(cell.getRegistryName(), new CellSymbol<>(cell, uSize, vSize, pins));
    }
}
