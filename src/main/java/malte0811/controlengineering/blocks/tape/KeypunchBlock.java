package malte0811.controlengineering.blocks.tape;

import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalStructurePlacement;
import malte0811.controlengineering.blocks.shapes.CachedShape;
import malte0811.controlengineering.blocks.shapes.DirectionalShapeProvider;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.gui.tape.KeypunchContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KeypunchBlock extends CEBlock<Direction> implements IModelOffsetProvider {
    public static final String CONTAINER_NAME = "screen.controlengineering.keypunch";
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final Property<Boolean> UPPER = BooleanProperty.create("upper");
    private static final VoxelShape BASE_SHAPE = Shapes.or(box(0, 0, 0, 16, 7, 8), box(0, 0, 8, 16, 2, 16));
    public static final CachedShape<Direction> SHAPE_PROVIDER = new DirectionalShapeProvider(
            FromBlockFunction.getProperty(FACING), BASE_SHAPE
    );

    public KeypunchBlock() {
        super(
                defaultPropertiesNotSolid(),
                HorizontalStructurePlacement.column2(FACING, UPPER),
                FromBlockFunction.either(
                        FromBlockFunction.getProperty(UPPER), FromBlockFunction.constant(Shapes.block()), SHAPE_PROVIDER
                ),
                CEBlockEntities.KEYPUNCH
        );
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, UPPER);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(
            @Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos
    ) {
        return new SimpleMenuProvider(
                (id, inv, player) -> new KeypunchContainer(
                        id, ContainerLevelAccess.create(worldIn, getMasterPos(state, pos))
                ),
                new TranslatableComponent(CONTAINER_NAME)
        );
    }

    public static boolean isMaster(BlockState state) {
        return !state.getValue(UPPER);
    }

    public static BlockPos getMasterPos(BlockState state, BlockPos here) {
        return state.getValue(UPPER) ? here.below() : here;
    }

    @Override
    public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size) {
        return state.getValue(UPPER) ? BlockPos.ZERO.above() : BlockPos.ZERO;
    }
}
