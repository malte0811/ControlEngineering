package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.cells.impl.Comparator;
import malte0811.controlengineering.logic.cells.impl.RSLatch;
import malte0811.controlengineering.logic.cells.impl.SchmittTrigger;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.List;

import static malte0811.controlengineering.logic.cells.PinDirection.DELAYED_OUTPUT;
import static malte0811.controlengineering.logic.schematic.symbol.SymbolPin.*;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SchematicSymbols {
    public static final TypedRegistry<SchematicSymbol<?>> REGISTRY = new TypedRegistry<>();
    public static final ResourceLocation SYMBOLS_SHEET = new ResourceLocation(
            ControlEngineering.MODID, "textures/gui/schematic_symbols.png"
    );
    public static final IOSymbol INPUT_PIN = new IOSymbol(true);
    public static final IOSymbol OUTPUT_PIN = new IOSymbol(false);
    public static final ConstantSymbol CONSTANT = new ConstantSymbol();

    @SubscribeEvent
    public static void init(FMLClientSetupEvent ev) {
        final int secondColumn = 13;
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

        registerSimpleCell(Leafcells.AND2, 0, 0, 9, twoInputPins);
        registerSimpleCell(Leafcells.AND3, 0, 14, 9, threeInputPinsFlush);

        registerSimpleCell(Leafcells.OR2, 0, 7, 11, twoInputPins);
        registerSimpleCell(Leafcells.OR3, 0, 21, 11, threeInputPinsShift);

        registerSimpleCell(Leafcells.NAND2, secondColumn, 0, 12, twoInputPins);
        registerSimpleCell(Leafcells.NAND3, secondColumn, 14, 12, threeInputPinsFlush);

        registerSimpleCell(Leafcells.NOR2, secondColumn, 7, 13, twoInputPins);
        registerSimpleCell(Leafcells.NOR3, secondColumn, 21, 13, threeInputPinsShift);

        registerSimpleCell(Leafcells.XOR2, 0, 28, 13, twoInputPins);
        registerSimpleCell(Leafcells.XOR3, 0, 35, 13, threeInputPinsShift);

        registerSimpleCell(
                Leafcells.NOT, secondColumn, 28, 13, ImmutableList.of(digitalIn(0, 3, LeafcellType.DEFAULT_IN_NAME))
        );
        registerCell(
                Leafcells.RS_LATCH, secondColumn, 49, 13, ImmutableList.of(
                        digitalIn(0, 1, RSLatch.SET),
                        digitalIn(0, 5, RSLatch.RESET),
                        new SymbolPin(12, 1, SignalType.DIGITAL, DELAYED_OUTPUT, RSLatch.Q),
                        new SymbolPin(12, 5, SignalType.DIGITAL, DELAYED_OUTPUT, RSLatch.NOT_Q)
                )
        );

        registerSimpleCell(
                Leafcells.SCHMITT_TRIGGER,
                0, 42, 13, ImmutableList.of(
                        analogIn(0, 1, SchmittTrigger.HIGH_PIN),
                        analogIn(0, 3, LeafcellType.DEFAULT_IN_NAME),
                        analogIn(0, 5, SchmittTrigger.LOW_PIN)
                )
        );
        registerSimpleCell(
                Leafcells.DIGITIZER,
                secondColumn,
                42,
                13,
                ImmutableList.of(analogIn(0, 3, LeafcellType.DEFAULT_IN_NAME))
        );
        registerSimpleCell(
                Leafcells.COMPARATOR, secondColumn, 35, 13, ImmutableList.of(
                        analogIn(0, 1, Comparator.NEGATIVE), analogIn(0, 5, Comparator.POSITIVE)
                )
        );

        delayCell(Leafcells.D_LATCH, 49, 10, SignalType.DIGITAL);
        delayCell(Leafcells.DELAY_LINE, 56, 13, SignalType.ANALOG);

        REGISTRY.register(new ResourceLocation(ControlEngineering.MODID, "input_pin"), INPUT_PIN);
        REGISTRY.register(new ResourceLocation(ControlEngineering.MODID, "output_pin"), OUTPUT_PIN);
        REGISTRY.register(new ResourceLocation(ControlEngineering.MODID, "constant"), CONSTANT);
    }

    private static void delayCell(LeafcellType<?> cell, int vMin, int uSize, SignalType type) {
        List<SymbolPin> pins = ImmutableList.of(
                new SymbolPin(0, 3, type, PinDirection.INPUT, LeafcellType.DEFAULT_IN_NAME),
                new SymbolPin(uSize - 1, 3, type, DELAYED_OUTPUT, LeafcellType.DEFAULT_OUT_NAME)
        );
        registerCell(cell, 0, vMin, uSize, pins);
    }

    private static void registerSimpleCell(
            LeafcellType<?> cell, int uMin, int vMin, int uSize, List<SymbolPin> inputPins
    ) {
        List<SymbolPin> allPins = new ArrayList<>(inputPins);
        allPins.add(digitalOut(uSize - 1, 3, LeafcellType.DEFAULT_OUT_NAME));
        registerCell(cell, uMin, vMin, uSize, allPins);
    }

    private static void registerCell(LeafcellType<?> cell, int uMin, int vMin, int uSize, List<SymbolPin> pins) {
        REGISTRY.register(cell.getRegistryName(), new CellSymbol(cell, uMin, vMin, uSize, 7, pins));
    }
}
