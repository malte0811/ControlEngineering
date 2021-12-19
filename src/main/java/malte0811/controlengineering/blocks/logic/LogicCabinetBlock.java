package malte0811.controlengineering.blocks.logic;

import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blockentity.logic.LogicCabinetBlockEntity;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalStructurePlacement;
import malte0811.controlengineering.blocks.shapes.DirectionalShapeProvider;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LogicCabinetBlock extends CEBlock<Direction, LogicCabinetBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", 0, 1);

    public static DirectionalShapeProvider BOTTOM_SHAPE = new DirectionalShapeProvider(
            FromBlockFunction.getProperty(FACING),
            Shapes.or(
                    Shapes.box(0, 0, 0, 1, 0.6875, 1),
                    Shapes.box(0, 0.6875, 0, 1, 1, 1 / 16.),
                    Shapes.box(0, 0.6875, 0, 1 / 16., 1, 1),
                    Shapes.box(15 / 16., 0.6875, 0, 1, 1, 1)
            )
    );
    public static DirectionalShapeProvider TOP_SHAPE = new DirectionalShapeProvider(
            FromBlockFunction.getProperty(FACING),
            Shapes.or(
                    Shapes.box(0, 0, 0, 1, 15 / 16., 1 / 16.),
                    Shapes.box(0, 0, 0, 1 / 16., 15 / 16., 1),
                    Shapes.box(15 / 16., 0, 0, 1, 15 / 16., 1),
                    Shapes.box(0, 15 / 16., 0, 1, 1, 1)
            )
    );

    public LogicCabinetBlock() {
        super(
                defaultPropertiesNotSolid(),
                HorizontalStructurePlacement.column(FACING, HEIGHT),
                FromBlockFunction.either((state, world, pos) -> state.getValue(HEIGHT) > 0, BOTTOM_SHAPE, TOP_SHAPE),
                CEBlockEntities.LOGIC_CABINET
        );
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, HEIGHT);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level pLevel, @Nonnull BlockState pState, @Nonnull BlockEntityType<T> pBlockEntityType
    ) {
        if (!pLevel.isClientSide)
            return CEBlockEntities.LOGIC_CABINET.makeMasterTicker(pBlockEntityType, LogicCabinetBlockEntity::tick);
        return null;
    }

    public static boolean isMaster(BlockState state) {
        return state.getValue(HEIGHT) == 0;
    }
}
