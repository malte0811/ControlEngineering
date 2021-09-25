package malte0811.controlengineering.blocks.placement;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPropertyPlacement<T extends Comparable<T>> implements PlacementBehavior<T> {
    private final Property<T> property;
    private final Function<BlockPlaceContext, T> getData;

    public BlockPropertyPlacement(Property<T> property, Function<BlockPlaceContext, T> getData) {
        this.property = property;
        this.getData = getData;
    }

    public static PlacementBehavior<Direction> horizontal(Property<Direction> property) {
        return new BlockPropertyPlacement<>(property, BlockPlaceContext::getHorizontalDirection);
    }

    public static PlacementBehavior<Direction> sixDirectional(Property<Direction> property) {
        return new BlockPropertyPlacement<>(property, ctx -> ctx.getClickedFace().getOpposite());
    }

    @Override
    public T getPlacementData(BlockPlaceContext ctx) {
        return getData.apply(ctx);
    }

    @Override
    public Pair<T, BlockPos> getPlacementDataAndOffset(BlockState state, BlockEntity te) {
        return Pair.of(state.getValue(property), BlockPos.ZERO);
    }

    @Override
    public Collection<BlockPos> getPlacementOffsets(T data) {
        return ImmutableList.of(BlockPos.ZERO);
    }

    @Override
    public BlockState getStateForOffset(Block owner, BlockPos offset, T data) {
        return owner.defaultBlockState().setValue(property, data);
    }

    @Override
    public boolean isValidAtOffset(BlockPos offset, BlockState state, BlockEntity te, T data) {
        return state.getValue(property) == data;
    }
}
