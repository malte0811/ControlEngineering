package malte0811.controlengineering.blocks.placement;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SidedColumnPlacement implements PlacementBehavior<Direction> {
    private final DirectionProperty facing;
    private final IntegerProperty columnHeight;
    private final List<BlockPos> offsets;

    public SidedColumnPlacement(DirectionProperty facing, IntegerProperty columnHeight) {
        this.facing = facing;
        this.columnHeight = columnHeight;
        this.offsets = IntStream.range(0, columnHeight.getAllowedValues().size())
                .mapToObj(h -> new BlockPos(0, h, 0))
                .collect(Collectors.toList());
    }

    @Override
    public Direction getPlacementData(BlockItemUseContext ctx) {
        return ctx.getPlacementHorizontalFacing();
    }

    @Override
    public Pair<Direction, BlockPos> getPlacementDataAndOffset(BlockState state, TileEntity te) {
        return Pair.of(state.get(facing), new BlockPos(0, state.get(columnHeight), 0));
    }

    @Override
    public Collection<BlockPos> getPlacementOffsets(Direction data) {
        return offsets;
    }

    @Override
    public BlockState getStateForOffset(Block owner, BlockPos offset, Direction data) {
        return owner.getDefaultState()
                .with(facing, data)
                .with(columnHeight, offset.getY());
    }

    @Override
    public boolean isValidAtOffset(BlockPos offset, BlockState state, TileEntity te, Direction data) {
        return state.hasProperty(facing) && state.hasProperty(columnHeight)
                && state.get(facing) == data
                && state.get(columnHeight) == offset.getY();
    }
}
