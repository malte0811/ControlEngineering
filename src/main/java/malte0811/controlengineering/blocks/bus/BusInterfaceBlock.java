package malte0811.controlengineering.blocks.bus;

import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.BlockPropertyPlacement;
import malte0811.controlengineering.blocks.shapes.DirectionalShapeProvider;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class BusInterfaceBlock extends CEBlock<Direction, malte0811.controlengineering.blockentity.bus.BusInterfaceBlock> {
    public static final Property<Direction> FACING = DirectionProperty.create("facing", Direction.values());
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            createPixelRelative(4, 4, 0, 12, 12, 2),
            createPixelRelative(5, 5, 2, 11, 11, 5),
            createPixelRelative(6, 6, 5, 10, 10, 9)
    );

    public BusInterfaceBlock() {
        super(
                defaultPropertiesNotSolid(),
                BlockPropertyPlacement.sixDirectional(FACING),
                new DirectionalShapeProvider(FromBlockFunction.getProperty(FACING), NORTH_SHAPE),
                CEBlockEntities.BUS_INTERFACE
        );
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
}
