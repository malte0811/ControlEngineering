package malte0811.controlengineering.blocks.placement;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.RegistryObject;

import java.util.Collection;

public interface PlacementBehavior<T> {
    static PlacementBehavior<Unit> simple(RegistryObject<? extends Block> block) {
        Lazy<BlockState> state = Lazy.of(() -> block.get().getDefaultState());
        return new PlacementBehavior<Unit>() {
            @Override
            public Unit getPlacementData(BlockItemUseContext ctx) {
                return Unit.INSTANCE;
            }

            @Override
            public Pair<Unit, BlockPos> getPlacementDataAndOffset(BlockState state, TileEntity te) {
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
                    TileEntity te,
                    Unit data
            ) {
                return BlockPos.ZERO.equals(offset) && actualState == state.get();
            }
        };
    }

    T getPlacementData(BlockItemUseContext ctx);

    Pair<T, BlockPos> getPlacementDataAndOffset(BlockState state, TileEntity te);

    Collection<BlockPos> getPlacementOffsets(T data);

    BlockState getStateForOffset(Block owner, BlockPos offset, T data);

    default void fillTileData(BlockPos offset, TileEntity te, T data, ItemStack item) {}

    boolean isValidAtOffset(BlockPos offset, BlockState state, TileEntity te, T data);
}
