package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;

import static malte0811.controlengineering.logic.schematic.symbol.SymbolPin.analog;
import static malte0811.controlengineering.logic.schematic.symbol.SymbolPin.digital;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SchematicSymbols {
    public static final TypedRegistry<SchematicSymbol<?>> REGISTRY = new TypedRegistry<>();
    public static final ResourceLocation SYMBOLS_SHEET = new ResourceLocation(
            ControlEngineering.MODID, "textures/gui/schematic_symbols.png"
    );

    @SubscribeEvent
    public static void init(FMLClientSetupEvent ev) {
        final int secondColumn = 13;
        List<SymbolPin> twoInputPins = ImmutableList.of(digital(0, 1), digital(0, 5));
        List<SymbolPin> threeInputPinsFlush = ImmutableList.of(digital(0, 1), digital(0, 3), digital(0, 5));
        List<SymbolPin> threeInputPinsShift = ImmutableList.of(digital(0, 1), digital(1, 3), digital(0, 5));

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

        registerStandardSymbol(Leafcells.NOT, secondColumn, 28, 13, ImmutableList.of(digital(0, 3)));
        //TODO RS latch
        registerStandardSymbol(Leafcells.SCHMITT_TRIGGER, 0, 42, 13, ImmutableList.of(analog(0, 3)));

        REGISTRY.register(new ResourceLocation(ControlEngineering.MODID, "input_pin"), new IOSymbol(true));
        REGISTRY.register(new ResourceLocation(ControlEngineering.MODID, "output_pin"), new IOSymbol(false));
    }

    private static void registerStandardSymbol(
            LeafcellType<?> cell,
            int uMin, int vMin, int uSize,
            List<SymbolPin> inputPins
    ) {
        REGISTRY.register(cell.getRegistryName(), new CellSymbol(
                cell, uMin, vMin, uSize, 7, inputPins, ImmutableList.of(new SymbolPin(uSize - 1, 3, SignalType.DIGITAL))
        ));
    }
}
