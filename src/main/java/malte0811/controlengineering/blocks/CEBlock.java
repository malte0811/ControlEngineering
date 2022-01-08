package malte0811.controlengineering.blocks;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blockentity.base.IHasMaster;
import malte0811.controlengineering.blockentity.base.INeighborChangeListener;
import malte0811.controlengineering.blocks.placement.PlacementBehavior;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.gui.CustomDataContainerProvider;
import malte0811.controlengineering.util.RaytraceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class CEBlock<PlacementData> extends Block implements EntityBlock {
    public final PlacementBehavior<PlacementData> placementBehavior;
    private final FromBlockFunction<VoxelShape> getShape;
    @Nullable
    private final BiFunction<BlockPos, BlockState, ? extends BlockEntity> beType;

    public <BE extends BlockEntity> CEBlock(
            Properties properties,
            PlacementBehavior<PlacementData> placement,
            FromBlockFunction<VoxelShape> getShape,
            @Nullable RegistryObject<BlockEntityType<BE>> beType
    ) {
        this(properties, placement, getShape, (bp, bs) -> beType.get().create(bp, bs));
    }

    public CEBlock(
            Properties properties,
            PlacementBehavior<PlacementData> placement,
            FromBlockFunction<VoxelShape> getShape,
            @Nullable BiFunction<BlockPos, BlockState, ? extends BlockEntity> beType
    ) {
        super(properties);
        this.placementBehavior = placement;
        this.getShape = getShape;
        this.beType = beType;
    }

    @Override
    public void onRemove(
            @Nonnull BlockState state,
            @Nonnull Level worldIn,
            @Nonnull BlockPos pos,
            @Nonnull BlockState newState,
            boolean isMoving
    ) {
        BlockEntity bEntity = worldIn.getBlockEntity(pos);
        if (bEntity instanceof IHasMaster hasMaster) {
            hasMaster.setCachedMaster(hasMaster.computeMasterBE(state));
        }
        Pair<PlacementData, BlockPos> dataAndOffset = placementBehavior.getPlacementDataAndOffset(state, bEntity);
        super.onRemove(state, worldIn, pos, newState, isMoving);
        for (BlockPos offset : placementBehavior.getPlacementOffsets(dataAndOffset.getFirst())) {
            BlockPos relative = offset.subtract(dataAndOffset.getSecond());
            if (!BlockPos.ZERO.equals(relative)) {
                //TODO stop callee from running this loop again
                BlockPos absolute = pos.offset(relative);
                BlockState offsetState = worldIn.getBlockState(absolute);
                BlockEntity offsetBE = worldIn.getBlockEntity(absolute);
                if (placementBehavior.isValidAtOffset(offset, offsetState, offsetBE, dataAndOffset.getFirst())) {
                    worldIn.setBlockAndUpdate(absolute, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    @Nonnull
    @Override
    public VoxelShape getShape(
            @Nonnull BlockState state,
            @Nonnull BlockGetter worldIn,
            @Nonnull BlockPos pos,
            @Nonnull CollisionContext context
    ) {
        return getShape.apply(state, worldIn, pos);
    }

    @Nonnull
    @Override
    public VoxelShape getVisualShape(
            @Nonnull BlockState state,
            @Nonnull BlockGetter reader,
            @Nonnull BlockPos pos,
            @Nonnull CollisionContext context
    ) {
        BlockEntity bEntity = reader.getBlockEntity(pos);
        if (bEntity instanceof SelectionShapeOwner shapeOwner) {
            VoxelShape selShape = shapeOwner.getShape().mainShape();
            if (selShape != null) {
                return selShape;
            }
        }
        return super.getVisualShape(state, reader, pos, context);
    }

    @Nonnull
    @Override
    public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos) {
        return state.getBlockSupportShape(worldIn, pos);
    }

    @Nonnull
    @Override
    public InteractionResult use(
            @Nonnull BlockState state,
            @Nonnull Level worldIn,
            @Nonnull BlockPos pos,
            @Nonnull Player player,
            @Nonnull InteractionHand handIn,
            @Nonnull BlockHitResult hit
    ) {
        if (worldIn.getBlockEntity(pos) instanceof SelectionShapeOwner shapeOwner) {
            return shapeOwner.getShape()
                    .onUse(
                            new UseOnContext(player, handIn, hit),
                            RaytraceUtils.create(player, 0, Vec3.atLowerCornerOf(pos))
                    );
        }
        return super.use(state, worldIn, pos, player, handIn, hit);
    }

    public void openContainer(Player player, BlockState state, Level worldIn, BlockPos pos) {
        if (player instanceof ServerPlayer serverPlayer) {
            MenuProvider container = state.getMenuProvider(worldIn, pos);
            if (container instanceof CustomDataContainerProvider customProvider) {
                customProvider.open(serverPlayer);
            } else {
                NetworkHooks.openGui(serverPlayer, container, pos);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState) {
        if (this.beType != null) {
            return this.beType.apply(pPos, pState);
        } else {
            return null;
        }
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, world, pos, neighbor);
        if (world.getBlockEntity(pos) instanceof INeighborChangeListener listener) {
            listener.onNeighborChanged(neighbor);
        }
    }

    public BlockPos getMainBlock(BlockState state, BlockEntity bEntity) {
        Pair<PlacementData, BlockPos> data = placementBehavior.getPlacementDataAndOffset(state, bEntity);
        return bEntity.getBlockPos().subtract(data.getSecond());
    }

    protected static BlockBehaviour.Properties defaultProperties() {
        return Properties.of(Material.METAL)
                .strength(3, 15)
                .sound(SoundType.METAL);
    }

    protected static BlockBehaviour.Properties defaultPropertiesNotSolid() {
        return defaultProperties().noOcclusion().isRedstoneConductor(($1, $2, $3) -> false).dynamicShape();
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity>
    BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> actual, RegistryObject<BlockEntityType<E>> expected, Consumer<E> ticker
    ) {
        return expected.get() == actual ? ($, $2, $3, bEntity) -> ticker.accept((E) bEntity) : null;
    }
}
