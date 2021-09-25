package malte0811.controlengineering.blocks.shapes;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class CachedShape<Key> implements FromBlockFunction<VoxelShape>, Function<Key, VoxelShape> {
    private final Cache<Key, VoxelShape> shapeCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build();

    @Override
    public VoxelShape apply(BlockState state, BlockGetter world, BlockPos pos) {
        Key k = getKey(state, world, pos);
        return apply(k);
    }

    @Override
    public VoxelShape apply(Key k) {
        VoxelShape present = shapeCache.getIfPresent(k);
        if (present == null) {
            present = compute(k);
            shapeCache.put(k, present);
        }
        return present;
    }

    protected abstract VoxelShape compute(Key k);

    protected abstract Key getKey(BlockState state, BlockGetter world, BlockPos pos);
}
