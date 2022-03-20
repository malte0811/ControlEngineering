package malte0811.controlengineering.logic.cells;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitOperator;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.impl.*;
import net.minecraft.resources.ResourceLocation;

public class Leafcells {
    public static final AssociativeFunctionCell AND2 = register(
            "and2",
            new AssociativeFunctionCell(2, LogicCircuitOperator.AND, true)
    );
    public static final AssociativeFunctionCell AND3 = register(
            "and3",
            new AssociativeFunctionCell(3, LogicCircuitOperator.AND, true)
    );
    public static final AssociativeFunctionCell OR2 = register(
            "or2",
            new AssociativeFunctionCell(2, LogicCircuitOperator.OR, false)
    );
    public static final AssociativeFunctionCell OR3 = register(
            "or3",
            new AssociativeFunctionCell(3, LogicCircuitOperator.OR, false)
    );
    public static final AssociativeFunctionCell XOR2 = register(
            "xor2",
            new AssociativeFunctionCell(2, LogicCircuitOperator.XOR, false)
    );
    public static final AssociativeFunctionCell XOR3 = register(
            "xor3",
            new AssociativeFunctionCell(3, LogicCircuitOperator.XOR, false)
    );
    public static final AssociativeFunctionCell NAND2 = register(
            "nand2",
            new InvertedAssociativeCell(2, LogicCircuitOperator.AND, true)
    );
    public static final AssociativeFunctionCell NAND3 = register(
            "nand3",
            new InvertedAssociativeCell(3, LogicCircuitOperator.AND, true)
    );
    public static final AssociativeFunctionCell NOR2 = register(
            "nor2",
            new InvertedAssociativeCell(2, LogicCircuitOperator.OR, false)
    );
    public static final AssociativeFunctionCell NOR3 = register(
            "nor3",
            new InvertedAssociativeCell(3, LogicCircuitOperator.OR, false)
    );
    public static final NotCell NOT = register("not", new NotCell());
    public static final RSLatch RS_LATCH = register("rs_latch", new RSLatch());
    public static final SchmittTrigger SCHMITT_TRIGGER = register("schmitt_trigger", new SchmittTrigger());
    public static final DelayCell DELAY_LINE = register("delay_line", new DelayCell(SignalType.ANALOG, 2, 10));
    public static final DelayCell D_LATCH = register("d_latch", new DelayCell(SignalType.DIGITAL, 3, 3));
    public static final Digitizer DIGITIZER = register("digitizer", new Digitizer());
    public static final Comparator COMPARATOR = register("comparator", new Comparator());

    private static <T extends LeafcellType<?>> T register(String name, T type) {
        return LeafcellType.register(new ResourceLocation(ControlEngineering.MODID, name), type);
    }
}
