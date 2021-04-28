package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.List;

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

        registerStandardSymbol(Leafcells.AND2, 0, 0, 9, twoInputPins);
        registerStandardSymbol(Leafcells.AND3, 0, 14, 9, threeInputPinsFlush);

        registerStandardSymbol(Leafcells.OR2, 0, 7, 11, twoInputPins);
        registerStandardSymbol(Leafcells.OR3, 0, 21, 11, threeInputPinsShift);

        registerStandardSymbol(Leafcells.NAND2, secondColumn, 0, 12, twoInputPins);
        registerStandardSymbol(Leafcells.NAND3, secondColumn, 14, 12, threeInputPinsFlush);

        registerStandardSymbol(Leafcells.NOR2, secondColumn, 7, 13, twoInputPins);
        registerStandardSymbol(Leafcells.NOR3, secondColumn, 21, 13, threeInputPinsShift);

        registerStandardSymbol(Leafcells.XOR2, 0, 28, 13, twoInputPins);
        registerStandardSymbol(Leafcells.XOR3, 0, 35, 13, threeInputPinsShift);

        registerStandardSymbol(
                Leafcells.NOT,
                secondColumn,
                28,
                13,
                ImmutableList.of(digitalIn(0, 3, LeafcellType.DEFAULT_IN_NAME))
        );
        //TODO RS latch
        registerStandardSymbol(
                Leafcells.SCHMITT_TRIGGER,
                0,
                42,
                13,
                ImmutableList.of(analogIn(0, 3, LeafcellType.DEFAULT_IN_NAME))
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
                new SymbolPin(uSize - 1, 3, type, PinDirection.DELAYED_OUTPUT, LeafcellType.DEFAULT_OUT_NAME)
        );
        REGISTRY.register(cell.getRegistryName(), new CellSymbol(cell, 0, vMin, uSize, 7, pins));
    }

    private static void registerStandardSymbol(
            LeafcellType<?> cell, int uMin, int vMin, int uSize, List<SymbolPin> inputPins
    ) {
        List<SymbolPin> allPins = new ArrayList<>(inputPins);
        allPins.add(digitalOut(uSize - 1, 3, LeafcellType.DEFAULT_OUT_NAME));
        REGISTRY.register(cell.getRegistryName(), new CellSymbol(cell, uMin, vMin, uSize, 7, allPins));
    }
}
