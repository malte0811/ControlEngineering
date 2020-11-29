package malte0811.controlengineering.blocks.panels;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalPlacement;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.tiles.panels.PanelCNCTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PanelCNCBlock extends CEBlock<Direction> {
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape SHAPE = VoxelShapes.or(
            VoxelShapes.create(0, 0, 0, 1, 2 / 16., 1),
            VoxelShapes.create(0, 12 / 16., 0, 1, 1, 1),

            VoxelShapes.create(0, 0, 0, 1 / 16., 1, 1 / 16.),
            VoxelShapes.create(15 / 16., 0, 0, 1, 1, 1 / 16.),
            VoxelShapes.create(15 / 16., 0, 15 / 16., 1, 1, 1),
            VoxelShapes.create(0, 0, 15 / 16., 1 / 16., 1, 1)
    );

    public PanelCNCBlock() {
        super(
                Properties.create(Material.IRON).notSolid(),
                new HorizontalPlacement(FACING),
                FromBlockFunction.constant(SHAPE)
        );
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PanelCNCTile();
    }

    public static Direction getDirection(PanelCNCTile tile) {
        return tile.getWorld().getBlockState(tile.getPos()).get(FACING);
    }
}
