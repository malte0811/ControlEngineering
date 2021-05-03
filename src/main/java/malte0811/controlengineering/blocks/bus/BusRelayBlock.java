package malte0811.controlengineering.blocks.bus;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.BlockPropertyPlacement;
import malte0811.controlengineering.blocks.shapes.DirectionalShapeProvider;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.bus.BusRelayTile;
import malte0811.controlengineering.util.DirectionUtils;
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

public class BusRelayBlock extends CEBlock<Direction, BusRelayTile> {
    public static final Property<Direction> FACING = DirectionProperty.create("facing", DirectionUtils.VALUES);
    private static final VoxelShape NORTH_SHAPE = VoxelShapes.or(
            createPixelRelative(4, 4, 0, 12, 12, 3),
            createPixelRelative(5, 5, 3, 11, 11, 5),
            createPixelRelative(6.5, 6.5, 5, 9.5, 9.5, 8)
    );

    public BusRelayBlock() {
        super(
                Properties.create(Material.IRON),
                BlockPropertyPlacement.sixDirectional(FACING),
                new DirectionalShapeProvider(FromBlockFunction.getProperty(FACING), NORTH_SHAPE),
                CETileEntities.BUS_RELAY
        );
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }
}
