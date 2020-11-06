package malte0811.controlengineering.blocks;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blocks.placement.PlacementBehavior;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;

public abstract class CEBlock<PlacementData> extends Block {
    public final PlacementBehavior<PlacementData> placementBehavior;
    private final FromBlockFunction<VoxelShape> getShape;

    public CEBlock(
            Properties properties,
            PlacementBehavior<PlacementData> placement,
            FromBlockFunction<VoxelShape> getShape
    ) {
        super(properties);
        this.placementBehavior = placement;
        this.getShape = getShape;
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

    @Nonnull
    @Override
    public VoxelShape getShape(
            @Nonnull BlockState state,
            @Nonnull IBlockReader worldIn,
            @Nonnull BlockPos pos,
            @Nonnull ISelectionContext context
    ) {
        return getShape.apply(state, worldIn, pos);
    }

    protected void openContainer(PlayerEntity player, BlockState state, World worldIn, BlockPos pos) {
        if (player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, state.getContainer(worldIn, pos), pos);
        }
    }
}
