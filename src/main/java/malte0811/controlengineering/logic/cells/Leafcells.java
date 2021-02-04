package malte0811.controlengineering.logic.cells;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.logic.cells.impl.AssociativeFunctionCell;
import malte0811.controlengineering.logic.cells.impl.InvertedAssociativeCell;
import malte0811.controlengineering.logic.cells.impl.RSLatch;
import net.minecraft.util.math.shapes.IBooleanFunction;

import java.util.HashMap;
import java.util.Map;

import static malte0811.controlengineering.logic.cells.LeafcellType.register;

public class Leafcells {
    public static final Map<Pair<IBooleanFunction, Integer>, AssociativeFunctionCell> BASIC_LOGIC = new HashMap<>();
    public static final RSLatch RS_LATCH = new RSLatch();

    public static void init() {
        for (int numIn = 2; numIn < 4; ++numIn) {
            BASIC_LOGIC.put(
                    Pair.of(IBooleanFunction.AND, numIn),
                    register(new AssociativeFunctionCell("and", numIn, IBooleanFunction.AND, true))
            );
            BASIC_LOGIC.put(
                    Pair.of(IBooleanFunction.NOT_AND, numIn),
                    register(new InvertedAssociativeCell("nand", numIn, IBooleanFunction.AND, true))
            );

            BASIC_LOGIC.put(
                    Pair.of(IBooleanFunction.OR, numIn),
                    register(new AssociativeFunctionCell("or", numIn, IBooleanFunction.OR, false))
            );
            BASIC_LOGIC.put(
                    Pair.of(IBooleanFunction.NOT_OR, numIn),
                    register(new InvertedAssociativeCell("nor", numIn, IBooleanFunction.OR, false))
            );

            BASIC_LOGIC.put(
                    Pair.of(IBooleanFunction.NOT_SAME, numIn),
                    register(new AssociativeFunctionCell("xor", numIn, IBooleanFunction.NOT_SAME, false))
            );
        }
        register(RS_LATCH);
    }
}
