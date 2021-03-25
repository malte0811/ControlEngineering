package malte0811.controlengineering.blocks.logic;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.SidedColumnPlacement;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.logic.LogicBoxTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShapes;

import javax.annotation.Nonnull;

public class LogicBoxBlock extends CEBlock<Direction, LogicBoxTile> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", 0, 1);

    public LogicBoxBlock() {
        super(
                Properties.create(Material.IRON).notSolid().setOpaque(($1, $2, $3) -> false),
                new SidedColumnPlacement(FACING, HEIGHT),
                // TODO proper shapes and caching
                (state, world, pos) -> VoxelShapes.create(0.125, 0.125, 0.125, 0.875, 0.875, 0.875),
                CETileEntities.LOGIC_BOX
        );
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING, HEIGHT);
    }
}
