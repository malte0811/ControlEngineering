package malte0811.controlengineering.blocks.shapes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

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
    public VoxelShape apply(BlockState state, BlockGetter world, BlockPos pos) {
        return apply(getKey.apply(state, world, pos), getFacing.apply(state, world, pos));
    }
}
