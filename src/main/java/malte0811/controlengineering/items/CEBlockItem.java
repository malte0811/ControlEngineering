package malte0811.controlengineering.items;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.PlacementBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class CEBlockItem<PlacementData> extends BlockItem {
    private final PlacementBehavior<PlacementData> placementBehavior;

    public CEBlockItem(CEBlock<PlacementData, ?> blockIn, Properties builder) {
        super(blockIn, builder);
        placementBehavior = blockIn.placementBehavior;
    }

    @Override
    protected boolean placeBlock(@Nonnull BlockPlaceContext context, @Nonnull BlockState ignored) {
        PlacementData data = placementBehavior.getPlacementData(context);
        Collection<BlockPos> offsets = placementBehavior.getPlacementOffsets(data);
        for (BlockPos offset : offsets) {
            BlockPos pos = context.getClickedPos().offset(offset);
            BlockState state = context.getLevel().getBlockState(pos);
            if (!state.canBeReplaced(BlockPlaceContext.at(context, pos, context.getClickedFace()))) {
                return false;
            }
        }
        for (BlockPos offset : offsets) {
            BlockPos pos = context.getClickedPos().offset(offset);
            BlockState stateToPlace = placementBehavior.getStateForOffset(getBlock(), offset, data);
            context.getLevel().setBlockAndUpdate(pos, stateToPlace);
            placementBehavior.fillTileData(offset, context.getLevel().getBlockEntity(pos), data, context.getItemInHand());
        }
        return true;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
            @Nonnull BlockPos pos,
            @Nonnull Level worldIn,
            @Nullable Player player,
            @Nonnull ItemStack stack,
            @Nonnull BlockState state
    ) {
        // Do not read NBT
        return false;
    }
}
