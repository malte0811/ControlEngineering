package malte0811.controlengineering.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BasicConnectorBlock<T extends TileEntity> extends Block {
    private final Supplier<TileEntityType<T>> tileType;

    public BasicConnectorBlock(Properties properties, Supplier<TileEntityType<T>> tileType) {
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
}
