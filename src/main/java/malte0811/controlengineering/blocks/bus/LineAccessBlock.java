package malte0811.controlengineering.blocks.bus;

import blusunrize.immersiveengineering.api.Lib;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.BlockPropertyPlacement;
import malte0811.controlengineering.blocks.shapes.DirectionalShapeProvider;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.bus.BusWireType;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.bus.LineAccessTile;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nonnull;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class LineAccessBlock extends CEBlock<Direction, LineAccessTile> {
    public static final Property<Direction> FACING = DirectionProperty.create(
            "facing", DirectionUtils.BY_HORIZONTAL_INDEX
    );
    private static final VoxelShape NORTH_SHAPE = VoxelShapes.or(
            createPixelRelative(5, 0, 1, 11, 2, 15),
            createPixelRelative(5, 2, 1, 11, 5, 7),
            createPixelRelative(6, 5, 2, 10, 9, 6),
            createPixelRelative(5, 2, 9, 11, 5, 15),
            createPixelRelative(6, 5, 10, 10, 9, 14)
    );
    public static final ToolType SCREWDRIVER_TOOL = ToolType.get(Lib.MODID + "_screwdriver");

    public LineAccessBlock() {
        super(
                defaultPropertiesNotSolid(),
                BlockPropertyPlacement.horizontal(FACING),
                new DirectionalShapeProvider(FromBlockFunction.getProperty(FACING), NORTH_SHAPE),
                CETileEntities.LINE_ACCESS
        );
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
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
        ItemStack held = player.getHeldItem(handIn);
        if (held.getToolTypes().contains(SCREWDRIVER_TOOL)) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof LineAccessTile) {
                LineAccessTile lineTile = (LineAccessTile) te;
                lineTile.selectedLine = (lineTile.selectedLine + 1) % BusWireType.NUM_LINES;
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }
}
