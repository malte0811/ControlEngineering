package malte0811.controlengineering.blocks;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blocks.placement.PlacementBehavior;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public abstract class CEBlock<PlacementData> extends Block {
    public final PlacementBehavior<PlacementData> placementBehavior;

    public CEBlock(Properties properties, PlacementBehavior<PlacementData> placement) {
        super(properties);
        this.placementBehavior = placement;
    }

    @Override
    public void onReplaced(
            @Nonnull BlockState state,
            @Nonnull World worldIn,
            @Nonnull BlockPos pos,
            @Nonnull BlockState newState,
            boolean isMoving
    ) {
        TileEntity te = worldIn.getTileEntity(pos);
        Pair<PlacementData, BlockPos> dataAndOffset = placementBehavior.getPlacementDataAndOffset(state, te);
        super.onReplaced(state, worldIn, pos, newState, isMoving);
        for (BlockPos offset : placementBehavior.getPlacementOffsets(dataAndOffset.getFirst())) {
            BlockPos relative = offset.subtract(dataAndOffset.getSecond());
            if (!BlockPos.ZERO.equals(relative)) {
                //TODO stop callee from running this loop again
                BlockPos absolute = pos.add(relative);
                BlockState offsetState = worldIn.getBlockState(absolute);
                TileEntity offsetTile = worldIn.getTileEntity(absolute);
                if (placementBehavior.isValidAtOffset(offset, offsetState, offsetTile, dataAndOffset.getFirst())) {
                    worldIn.setBlockState(absolute, Blocks.AIR.getDefaultState());
                }
            }
        }
    }
}
