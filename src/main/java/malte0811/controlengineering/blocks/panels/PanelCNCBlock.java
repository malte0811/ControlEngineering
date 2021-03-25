package malte0811.controlengineering.blocks.panels;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalPlacement;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.panels.PanelCNCTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import javax.annotation.Nonnull;

public class PanelCNCBlock extends CEBlock<Direction, PanelCNCTile> {
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape BASE = VoxelShapes.create(0, 0, 0, 1, 2 / 16., 1);
    public static final VoxelShape TOP = VoxelShapes.create(0, 12 / 16., 0, 1, 1, 1);
    public static final VoxelShape SHAPE = VoxelShapes.or(
            BASE,
            TOP,

            VoxelShapes.create(0, 0, 0, 1 / 16., 1, 1 / 16.),
            VoxelShapes.create(15 / 16., 0, 0, 1, 1, 1 / 16.),
            VoxelShapes.create(15 / 16., 0, 15 / 16., 1, 1, 1),
            VoxelShapes.create(0, 0, 15 / 16., 1 / 16., 1, 1)
    );

    public PanelCNCBlock() {
        super(
                Properties.create(Material.IRON).notSolid(),
                new HorizontalPlacement(FACING),
                FromBlockFunction.constant(SHAPE),
                CETileEntities.PANEL_CNC
        );
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }

    public static Direction getDirection(PanelCNCTile tile) {
        return tile.getWorld().getBlockState(tile.getPos()).get(FACING);
    }
}
