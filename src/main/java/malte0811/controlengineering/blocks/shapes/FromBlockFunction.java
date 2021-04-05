package malte0811.controlengineering.blocks.shapes;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.Map;

@FunctionalInterface
public interface FromBlockFunction<T> {
    T apply(BlockState state, IBlockReader world, BlockPos pos);

    static <T extends Comparable<T>> FromBlockFunction<T> getProperty(Property<T> prop) {
        return (state, w, p) -> state.get(prop);
    }

    static <T> FromBlockFunction<T> either(
            FromBlockFunction<Boolean> useSecond, FromBlockFunction<T> first, FromBlockFunction<T> second
    ) {
        return switchOn(useSecond, ImmutableMap.of(false, first, true, second));
    }

    static <T, T2 extends Comparable<T2>>
    FromBlockFunction<T> switchOnProperty(Property<T2> prop, Map<T2, FromBlockFunction<T>> subFunctions) {
        return switchOn((state, world, pos) -> state.get(prop), subFunctions);
    }

    static <T, T2 extends Comparable<T2>>
    FromBlockFunction<T> switchOn(FromBlockFunction<T2> prop, Map<T2, FromBlockFunction<T>> subFunctions) {
        return (state, world, pos) -> subFunctions.get(prop.apply(state, world, pos))
                .apply(state, world, pos);
    }

    static <T> FromBlockFunction<T> constant(T value) {
        return (s, w, p) -> value;
    }
}
