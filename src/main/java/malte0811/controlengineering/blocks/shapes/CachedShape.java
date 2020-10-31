package malte0811.controlengineering.blocks.shapes;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public abstract class CachedShape<Key> implements FromBlockFunction<VoxelShape> {
    private final Cache<Key, VoxelShape> shapeCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build();
    private final FromBlockFunction<Key> getKey;

    protected CachedShape(FromBlockFunction<Key> getKey) {
        this.getKey = getKey;
    }

    @Override
    public VoxelShape apply(BlockState state, IBlockReader world, BlockPos pos) {
        Key k = getKey.apply(state, world, pos);
        VoxelShape present = shapeCache.getIfPresent(k);
        if (present == null) {
            present = compute(k);
            shapeCache.put(k, present);
        }
        return present;
    }

    protected abstract VoxelShape compute(Key k);
}
