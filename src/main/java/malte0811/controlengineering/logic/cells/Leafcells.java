package malte0811.controlengineering.logic.cells;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.impl.AssociativeFunctionCell;
import malte0811.controlengineering.logic.cells.impl.InvertedAssociativeCell;
import malte0811.controlengineering.logic.cells.impl.RSLatch;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.IBooleanFunction;

import java.util.HashMap;
import java.util.Map;


public class Leafcells {
    public static final Map<Pair<IBooleanFunction, Integer>, AssociativeFunctionCell> BASIC_LOGIC = new HashMap<>();
    public static final RSLatch RS_LATCH = new RSLatch();

    public static void init() {
        for (int numIn = 2; numIn < 4; ++numIn) {
            BASIC_LOGIC.put(
                    Pair.of(IBooleanFunction.AND, numIn),
                    register(
                            "and" + numIn,
                            new AssociativeFunctionCell(numIn, IBooleanFunction.AND, true, (1 + numIn) / 2.)
                    )
            );
            BASIC_LOGIC.put(
                    Pair.of(IBooleanFunction.NOT_AND, numIn),
                    register("nand" + numIn, new InvertedAssociativeCell(numIn, IBooleanFunction.AND, true, numIn / 2.))
            );

            BASIC_LOGIC.put(
                    Pair.of(IBooleanFunction.OR, numIn),
                    register(
                            "or" + numIn,
                            new AssociativeFunctionCell(numIn, IBooleanFunction.OR, false, (1 + numIn) / 2.)
                    )
            );
            BASIC_LOGIC.put(
                    Pair.of(IBooleanFunction.NOT_OR, numIn),
                    register("nor" + numIn, new InvertedAssociativeCell(numIn, IBooleanFunction.OR, false, numIn / 2.))
            );

            BASIC_LOGIC.put(
                    Pair.of(IBooleanFunction.NOT_SAME, numIn),
                    register(
                            "xor" + numIn, new AssociativeFunctionCell(numIn, IBooleanFunction.NOT_SAME, false, numIn)
                    )
            );
        }
        register("rs_latch", RS_LATCH);
    }

    private static <T extends LeafcellType<?>> T register(String name, T type) {
        return LeafcellType.register(new ResourceLocation(ControlEngineering.MODID, name), type);
    }
}
