package malte0811.controlengineering.blocks.bus;

import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.BlockPropertyPlacement;
import malte0811.controlengineering.blocks.shapes.DirectionalShapeProvider;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.util.ShapeUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScopeBlock extends CEBlock<Direction> {
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    // TODO
    private static final VoxelShape NORTH_SHAPE = ShapeUtils.createPixelRelative(0, 0, 0, 16, 16, 15);

    public ScopeBlock() {
        super(
                defaultPropertiesNotSolid(),
                BlockPropertyPlacement.horizontal(FACING),
                new DirectionalShapeProvider(FromBlockFunction.getProperty(FACING), NORTH_SHAPE),
                CEBlockEntities.SCOPE
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
}
