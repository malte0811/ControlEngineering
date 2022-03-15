package malte0811.controlengineering.blocks.bus;

import blusunrize.immersiveengineering.api.IETags;
import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blockentity.bus.RSRemapperBlockEntity;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.BlockPropertyPlacement;
import malte0811.controlengineering.blocks.shapes.DirectionalShapeProvider;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.Random;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class RSRemapperBlock extends CEBlock<Direction> {
    public static final Property<Direction> FACING = DirectionProperty.create(
            "facing", DirectionUtils.BY_HORIZONTAL_INDEX
    );
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            createPixelRelative(0, 0, 1, 16, 2, 15),
            createPixelRelative(5, 2, 1, 11, 6, 7),
            createPixelRelative(6, 6, 2, 10, 10, 6),
            createPixelRelative(5, 2, 9, 11, 6, 15),
            createPixelRelative(6, 6, 10, 10, 10, 14)
    );

    public RSRemapperBlock() {
        super(
                defaultPropertiesNotSolid(),
                BlockPropertyPlacement.horizontal(FACING),
                new DirectionalShapeProvider(FromBlockFunction.getProperty(FACING), NORTH_SHAPE),
                CEBlockEntities.RS_REMAPPER
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
            @Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos,
            @Nonnull Player player, @Nonnull InteractionHand handIn, @Nonnull BlockHitResult hit
    ) {
        var held = player.getItemInHand(handIn);
        if (held.is(IETags.screwdrivers) && worldIn.getBlockEntity(pos) instanceof RSRemapperBlockEntity remapper) {
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openGui(serverPlayer, new SimpleMenuProvider(
                        CEContainers.RS_REMAPPER.argConstructor(remapper), TextComponent.EMPTY
                ));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void tick(
            @Nonnull BlockState state, @Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nonnull Random random
    ) {
        super.tick(state, level, pos, random);
        if (!(level.getBlockEntity(pos) instanceof RSRemapperBlockEntity remapper))
            return;
        remapper.onBlockTick();
    }
}
