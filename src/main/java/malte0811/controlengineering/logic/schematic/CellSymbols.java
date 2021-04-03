package malte0811.controlengineering.logic.schematic;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CellSymbols {
    public static final List<SchematicSymbol> SELECTABLE_SYMBOLS = new ArrayList<>();

    @SubscribeEvent
    public static void init(FMLClientSetupEvent ev) {
        final int secondColumn = 13;
        registerSymbol(Leafcells.AND2, 0, 0, 9, 7);
        registerSymbol(Leafcells.AND3, 0, 14, 9, 7);

        registerSymbol(Leafcells.OR2, 0, 7, 11, 7);
        registerSymbol(Leafcells.OR3, 0, 21, 11, 7);

        registerSymbol(Leafcells.NAND2, secondColumn, 0, 12, 7);
        registerSymbol(Leafcells.NAND3, secondColumn, 0, 12, 7);

        registerSymbol(Leafcells.NOR2, secondColumn, 7, 13, 7);
        registerSymbol(Leafcells.NOR3, secondColumn, 7, 13, 7);

        registerSymbol(Leafcells.XOR2, 0, 28, 13, 7);
        registerSymbol(Leafcells.XOR3, 0, 35, 13, 7);

        registerSymbol(Leafcells.NOT, secondColumn, 28, 13, 7);
    }

    private static void registerSymbol(LeafcellType<?> cell, int uMin, int vMin, int uSize, int vSize) {
        SELECTABLE_SYMBOLS.add(new CellSymbol(cell, uMin, vMin, uSize, vSize));
    }
}
