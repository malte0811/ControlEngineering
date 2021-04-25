package malte0811.controlengineering.blocks.shapes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

import java.util.Map;
import java.util.function.BiFunction;

public class HorizontalWithExtraShape<T> implements BiFunction<T, Direction, VoxelShape>,
        FromBlockFunction<VoxelShape> {
    private final FromBlockFunction<T> getKey;
    private final FromBlockFunction<Direction> getFacing;
    private final Map<T, DirectionalShapeProvider> shape;

    public HorizontalWithExtraShape(
            FromBlockFunction<T> getKey, FromBlockFunction<Direction> getFacing, Map<T, VoxelShape> shapes
    ) {
        this.getKey = getKey;
        this.getFacing = getFacing;
        this.shape = shapes.entrySet().stream()
                .map(p -> Pair.of(p.getKey(), new DirectionalShapeProvider(getFacing, p.getValue())))
                .collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
    }

    @Override
    public VoxelShape apply(T key, Direction direction) {
        return shape.get(key).apply(direction);
    }

    @Override
    public VoxelShape apply(BlockState state, IBlockReader world, BlockPos pos) {
        return apply(getKey.apply(state, world, pos), getFacing.apply(state, world, pos));
    }
}
