package malte0811.controlengineering.blocks;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blocks.placement.PlacementBehavior;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.util.RaytraceUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
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

    @Nonnull
    @Override
    public VoxelShape getRayTraceShape(
            @Nonnull BlockState state,
            @Nonnull IBlockReader reader,
            @Nonnull BlockPos pos,
            @Nonnull ISelectionContext context
    ) {
        TileEntity tile = reader.getTileEntity(pos);
        if (tile instanceof SelectionShapeOwner) {
            VoxelShape selShape = ((SelectionShapeOwner) tile).getShape().mainShape();
            if (selShape != null) {
                return selShape;
            }
        }
        return super.getRayTraceShape(state, reader, pos, context);
    }

    @Nonnull
    @Override
    public VoxelShape getRenderShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
        return state.getRenderShape(worldIn, pos);
    }

    @Nonnull
    @Override
    public ActionResultType onBlockActivated(
            @Nonnull BlockState state,
            @Nonnull World worldIn,
            @Nonnull BlockPos pos,
            @Nonnull PlayerEntity player,
            @Nonnull Hand handIn,
            @Nonnull BlockRayTraceResult hit
    ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof SelectionShapeOwner) {
            return ((SelectionShapeOwner) tile).getShape()
                    .onUse(
                            new ItemUseContext(player, handIn, hit),
                            RaytraceUtils.create(player, 0, Vector3d.copy(pos))
                    );
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    public void openContainer(PlayerEntity player, BlockState state, World worldIn, BlockPos pos) {
        if (player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, state.getContainer(worldIn, pos), pos);
        }
    }
}
