package malte0811.controlengineering.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BasicTileBlock<T extends TileEntity> extends Block {
    private final Supplier<TileEntityType<T>> tileType;

    public BasicTileBlock(Properties properties, Supplier<TileEntityType<T>> tileType) {
        super(properties.notSolid());
        this.tileType = tileType;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return tileType.get().create();
    }

    @Override
    public void neighborChanged(
            @Nonnull BlockState state,
            @Nonnull World worldIn,
            @Nonnull BlockPos pos,
            @Nonnull Block blockIn,
            @Nonnull BlockPos fromPos,
            boolean isMoving
    ) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof INeighborChangeListener) {
            ((INeighborChangeListener) te).onNeighborChanged(pos);
        }
    }
}
