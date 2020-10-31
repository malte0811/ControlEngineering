package malte0811.controlengineering.blocks.shapes;

import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

@FunctionalInterface
public interface FromBlockFunction<T> {
    T apply(BlockState state, IBlockReader world, BlockPos pos);

    static <T extends Comparable<T>> FromBlockFunction<T> getProperty(Property<T> prop) {
        return (state, w, p) -> state.get(prop);
    }

    static <T> FromBlockFunction<T> either(
            FromBlockFunction<Boolean> useSecond, FromBlockFunction<T> first, FromBlockFunction<T> second
    ) {
        return (state, world, pos) -> {
            if (useSecond.apply(state, world, pos)) {
                return second.apply(state, world, pos);
            } else {
                return first.apply(state, world, pos);
            }
        };
    }

    static <T> FromBlockFunction<T> constant(T value) {
        return (s, w, p) -> value;
    }
}
