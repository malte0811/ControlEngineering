package malte0811.controlengineering.blocks.placement;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public class HorizontalPlacement implements PlacementBehavior<Direction> {
    private final Property<Direction> property;

    public HorizontalPlacement(Property<Direction> property) {
        this.property = property;
    }

    @Override
    public Direction getPlacementData(BlockItemUseContext ctx) {
        return ctx.getPlacementHorizontalFacing();
    }

    @Override
    public Pair<Direction, BlockPos> getPlacementDataAndOffset(BlockState state, TileEntity te) {
        return Pair.of(state.get(property), BlockPos.ZERO);
    }

    @Override
    public Collection<BlockPos> getPlacementOffsets(Direction data) {
        return ImmutableList.of(BlockPos.ZERO);
    }

    @Override
    public BlockState getStateForOffset(Block owner, BlockPos offset, Direction data) {
        return owner.getDefaultState().with(property, data);
    }

    @Override
    public boolean isValidAtOffset(BlockPos offset, BlockState state, TileEntity te, Direction data) {
        return state.get(property) == data;
    }
}
