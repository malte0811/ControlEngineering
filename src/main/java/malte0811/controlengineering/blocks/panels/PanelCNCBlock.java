package malte0811.controlengineering.blocks.panels;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.BlockPropertyPlacement;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.panels.PanelCNCTile;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PanelCNCBlock extends CEBlock<Direction, PanelCNCTile> {
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape BASE = Shapes.box(0, 0, 0, 1, 2 / 16., 1);
    public static final VoxelShape TOP = Shapes.box(0, 12 / 16., 0, 1, 1, 1);
    public static final VoxelShape SHAPE = Shapes.or(
            BASE,
            TOP,

            Shapes.box(0, 0, 0, 1 / 16., 1, 1 / 16.),
            Shapes.box(15 / 16., 0, 0, 1, 1, 1 / 16.),
            Shapes.box(15 / 16., 0, 15 / 16., 1, 1, 1),
            Shapes.box(0, 0, 15 / 16., 1 / 16., 1, 1)
    );

    public PanelCNCBlock() {
        super(
                defaultPropertiesNotSolid(),
                BlockPropertyPlacement.horizontal(FACING),
                FromBlockFunction.constant(SHAPE),
                CETileEntities.PANEL_CNC
        );
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    public static Direction getDirection(PanelCNCTile tile) {
        return tile.getBlockState().getValue(FACING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @Nonnull Level pLevel, @Nonnull BlockState pState, @Nonnull BlockEntityType<T> pBlockEntityType
    ) {
        return createTickerHelper(pBlockEntityType, PanelCNCTile::tick);
    }
}
