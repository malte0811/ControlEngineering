package malte0811.controlengineering.blocks.bus;

import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.BlockPropertyPlacement;
import malte0811.controlengineering.blocks.shapes.DirectionalShapeProvider;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.util.ShapeUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScopeBlock extends CEBlock<Direction> {
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final DirectionalShapeProvider SHAPE = new DirectionalShapeProvider(
            FromBlockFunction.getProperty(FACING),
            ShapeUtils.createPixelRelative(0, 0, 0, 16, 16, 15)
    );

    public ScopeBlock() {
        super(
                defaultPropertiesNotSolid(),
                BlockPropertyPlacement.horizontal(FACING),
                SHAPE,
                CEBlockEntities.SCOPE
        );
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> actualType
    ) {
        if (!level.isClientSide) {
            return createTickerHelper(actualType, CEBlockEntities.SCOPE, ScopeBlockEntity::tickServer);
        } else {
            return null;
        }
    }
}
