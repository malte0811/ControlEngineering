package malte0811.controlengineering.items;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.PlacementBehavior;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    protected boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState ignored) {
        PlacementData data = placementBehavior.getPlacementData(context);
        Collection<BlockPos> offsets = placementBehavior.getPlacementOffsets(data);
        for (BlockPos offset : offsets) {
            BlockPos pos = context.getPos().add(offset);
            BlockState state = context.getWorld().getBlockState(pos);
            if (!state.isReplaceable(BlockItemUseContext.func_221536_a(context, pos, context.getFace()))) {
                return false;
            }
        }
        for (BlockPos offset : offsets) {
            BlockPos pos = context.getPos().add(offset);
            BlockState stateToPlace = placementBehavior.getStateForOffset(getBlock(), offset, data);
            context.getWorld().setBlockState(pos, stateToPlace);
            placementBehavior.fillTileData(offset, context.getWorld().getTileEntity(pos), data, context.getItem());
        }
        return true;
    }

    @Override
    protected boolean onBlockPlaced(
            @Nonnull BlockPos pos,
            @Nonnull World worldIn,
            @Nullable PlayerEntity player,
            @Nonnull ItemStack stack,
            @Nonnull BlockState state
    ) {
        // Do not read NBT
        return false;
    }
}
