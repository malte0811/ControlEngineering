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
import java.util.function.Function;

public class BlockPropertyPlacement<T extends Comparable<T>> implements PlacementBehavior<T> {
    private final Property<T> property;
    private final Function<BlockItemUseContext, T> getData;

    public BlockPropertyPlacement(Property<T> property, Function<BlockItemUseContext, T> getData) {
        this.property = property;
        this.getData = getData;
    }

    public static PlacementBehavior<Direction> horizontal(Property<Direction> property) {
        return new BlockPropertyPlacement<>(property, BlockItemUseContext::getPlacementHorizontalFacing);
    }

    public static PlacementBehavior<Direction> sixDirectional(Property<Direction> property) {
        return new BlockPropertyPlacement<>(property, ctx -> ctx.getFace().getOpposite());
    }

    @Override
    public T getPlacementData(BlockItemUseContext ctx) {
        return getData.apply(ctx);
    }

    @Override
    public Pair<T, BlockPos> getPlacementDataAndOffset(BlockState state, TileEntity te) {
        return Pair.of(state.get(property), BlockPos.ZERO);
    }

    @Override
    public Collection<BlockPos> getPlacementOffsets(T data) {
        return ImmutableList.of(BlockPos.ZERO);
    }

    @Override
    public BlockState getStateForOffset(Block owner, BlockPos offset, T data) {
        return owner.getDefaultState().with(property, data);
    }

    @Override
    public boolean isValidAtOffset(BlockPos offset, BlockState state, TileEntity te, T data) {
        return state.get(property) == data;
    }
}
