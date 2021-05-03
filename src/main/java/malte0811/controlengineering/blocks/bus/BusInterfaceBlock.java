package malte0811.controlengineering.blocks.bus;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.BlockPropertyPlacement;
import malte0811.controlengineering.blocks.shapes.DirectionalShapeProvider;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.bus.BusInterfaceTile;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import javax.annotation.Nonnull;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class BusInterfaceBlock extends CEBlock<Direction, BusInterfaceTile> {
    public static final Property<Direction> FACING = DirectionProperty.create("facing", Direction.values());
    private static final VoxelShape NORTH_SHAPE = VoxelShapes.or(
            createPixelRelative(4, 4, 0, 12, 12, 2),
            createPixelRelative(5, 5, 2, 11, 11, 5),
            createPixelRelative(6, 6, 5, 10, 10, 9)
    );

    public BusInterfaceBlock() {
        super(
                AbstractBlock.Properties.create(Material.IRON),
                BlockPropertyPlacement.sixDirectional(FACING),
                new DirectionalShapeProvider(FromBlockFunction.getProperty(FACING), NORTH_SHAPE),
                CETileEntities.BUS_INTERFACE
        );
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }
}
