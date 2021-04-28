package malte0811.controlengineering.logic.cells;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.impl.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.IBooleanFunction;


public class Leafcells {
    // TODO adjust tube count to what IE will use, add scaling
    public static final AssociativeFunctionCell AND2 = new AssociativeFunctionCell(2, IBooleanFunction.AND, true, 3);
    public static final AssociativeFunctionCell AND3 = new AssociativeFunctionCell(3, IBooleanFunction.AND, true, 4);
    public static final AssociativeFunctionCell OR2 = new AssociativeFunctionCell(2, IBooleanFunction.OR, false, 3);
    public static final AssociativeFunctionCell OR3 = new AssociativeFunctionCell(3, IBooleanFunction.OR, false, 4);
    public static final AssociativeFunctionCell XOR2 = new AssociativeFunctionCell(
            2, IBooleanFunction.NOT_SAME, false, 5
    );
    public static final AssociativeFunctionCell XOR3 = new AssociativeFunctionCell(
            3, IBooleanFunction.NOT_SAME, false, 8
    );
    public static final AssociativeFunctionCell NAND2 = new InvertedAssociativeCell(2, IBooleanFunction.AND, true, 1);
    public static final AssociativeFunctionCell NAND3 = new InvertedAssociativeCell(3, IBooleanFunction.AND, true, 2);
    public static final AssociativeFunctionCell NOR2 = new InvertedAssociativeCell(2, IBooleanFunction.OR, false, 1);
    public static final AssociativeFunctionCell NOR3 = new InvertedAssociativeCell(3, IBooleanFunction.OR, false, 2);
    public static final NotCell NOT = new NotCell();
    public static final RSLatch RS_LATCH = new RSLatch();
    public static final SchmittTrigger SCHMITT_TRIGGER = new SchmittTrigger();
    public static final DelayCell DELAY_LINE = new DelayCell(SignalType.ANALOG, 2);
    public static final DelayCell D_LATCH = new DelayCell(SignalType.DIGITAL, 2);
    public static final Digitizer DIGITIZER = new Digitizer();
    public static final Comparator COMPARATOR = new Comparator();

    public static void init() {
        register("and2", AND2);
        register("and3", AND3);
        register("or2", OR2);
        register("or3", OR3);
        register("xor2", XOR2);
        register("xor3", XOR3);
        register("nand2", NAND2);
        register("nand3", NAND3);
        register("nor2", NOR2);
        register("nor3", NOR3);
        register("not", NOT);
        register("rs_latch", RS_LATCH);
        register("schmitt_trigger", SCHMITT_TRIGGER);
        register("digitizer", DIGITIZER);
        register("comparator", COMPARATOR);
        register("delay_line", DELAY_LINE);
        register("d_latch", D_LATCH);
    }

    private static <T extends LeafcellType<?>> T register(String name, T type) {
        return LeafcellType.register(new ResourceLocation(ControlEngineering.MODID, name), type);
    }
}
