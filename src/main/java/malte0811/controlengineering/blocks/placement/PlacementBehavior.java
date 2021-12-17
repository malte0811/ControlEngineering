package malte0811.controlengineering.blocks.placement;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;

public interface PlacementBehavior<T> {
    static PlacementBehavior<Unit> simple(RegistryObject<? extends Block> block) {
        Lazy<BlockState> state = Lazy.of(() -> block.get().defaultBlockState());
        return new PlacementBehavior<>() {
            @Override
            public Unit getPlacementData(BlockPlaceContext ctx) {
                return Unit.INSTANCE;
            }

            @Override
            public Pair<Unit, BlockPos> getPlacementDataAndOffset(BlockState state, BlockEntity be) {
                return Pair.of(Unit.INSTANCE, BlockPos.ZERO);
            }

            @Override
            public Collection<BlockPos> getPlacementOffsets(Unit data) {
                return ImmutableList.of(BlockPos.ZERO);
            }

            @Override
            public BlockState getStateForOffset(Block owner, BlockPos offset, Unit data) {
                return state.get();
            }

            @Override
            public boolean isValidAtOffset(
                    BlockPos offset,
                    BlockState actualState,
                    BlockEntity be,
                    Unit data
            ) {
                return BlockPos.ZERO.equals(offset) && actualState == state.get();
            }
        };
    }

    T getPlacementData(BlockPlaceContext ctx);

    Pair<T, BlockPos> getPlacementDataAndOffset(BlockState state, BlockEntity be);

    Collection<BlockPos> getPlacementOffsets(T data);

    BlockState getStateForOffset(Block owner, BlockPos offset, T data);

    default void fillBEData(BlockPos offset, BlockEntity be, T data, ItemStack item) {}

    boolean isValidAtOffset(BlockPos offset, BlockState state, BlockEntity be, T data);
}
