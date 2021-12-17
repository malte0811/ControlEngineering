package malte0811.controlengineering.blocks.bus;

import blusunrize.immersiveengineering.api.IETags;
import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blockentity.bus.LineAccessBlockEntity;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.BlockPropertyPlacement;
import malte0811.controlengineering.blocks.shapes.DirectionalShapeProvider;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.bus.BusWireType;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class LineAccessBlock extends CEBlock<Direction, LineAccessBlockEntity> {
    public static final Property<Direction> FACING = DirectionProperty.create(
            "facing", DirectionUtils.BY_HORIZONTAL_INDEX
    );
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            createPixelRelative(5, 0, 1, 11, 2, 15),
            createPixelRelative(5, 2, 1, 11, 5, 7),
            createPixelRelative(6, 5, 2, 10, 9, 6),
            createPixelRelative(5, 2, 9, 11, 5, 15),
            createPixelRelative(6, 5, 10, 10, 9, 14)
    );

    public LineAccessBlock() {
        super(
                defaultPropertiesNotSolid(),
                BlockPropertyPlacement.horizontal(FACING),
                new DirectionalShapeProvider(FromBlockFunction.getProperty(FACING), NORTH_SHAPE),
                CEBlockEntities.LINE_ACCESS
        );
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
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
        ItemStack held = player.getItemInHand(handIn);
        if (held.is(IETags.screwdrivers) && worldIn.getBlockEntity(pos) instanceof LineAccessBlockEntity lineBE) {
            lineBE.selectedLine = (lineBE.selectedLine + 1) % BusWireType.NUM_LINES;
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
