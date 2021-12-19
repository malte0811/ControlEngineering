package malte0811.controlengineering.blocks.panels;

import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blockentity.panels.PanelCNCBlockEntity;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalStructurePlacement;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class PanelCNCBlock extends CEBlock<Direction, PanelCNCBlockEntity> implements IModelOffsetProvider {
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final Property<Integer> HEIGHT = IntegerProperty.create("height", 0, 1);

    public static final VoxelShape UPPER_SHAPE = Shapes.or(
            createPixelRelative(0, 0, 0, 1, 16, 1),
            createPixelRelative(15, 0, 0, 16, 16, 1),
            createPixelRelative(15, 0, 15, 16, 16, 16),
            createPixelRelative(0, 0, 15, 1, 16, 16),
            createPixelRelative(0, 10, 0, 16, 16, 16)
    );

    public PanelCNCBlock() {
        super(
                defaultPropertiesNotSolid(),
                HorizontalStructurePlacement.column(FACING, HEIGHT),
                FromBlockFunction.either((state, $, $1) -> state.getValue(HEIGHT) == 1, Shapes.block(), UPPER_SHAPE),
                CEBlockEntities.PANEL_CNC
        );
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, HEIGHT);
    }

    public static Direction getDirection(PanelCNCBlockEntity bEntity) {
        return bEntity.getBlockState().getValue(FACING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @Nonnull Level pLevel, @Nonnull BlockState pState, @Nonnull BlockEntityType<T> pBlockEntityType
    ) {
        return CEBlockEntities.PANEL_CNC.makeMasterTicker(pBlockEntityType, PanelCNCBlockEntity::tick);
    }

    public static boolean isMaster(BlockState blockState) {
        return blockState.getValue(HEIGHT) == 0;
    }

    @Override
    public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size) {
        return new BlockPos(0, state.getValue(HEIGHT), 0);
    }
}
