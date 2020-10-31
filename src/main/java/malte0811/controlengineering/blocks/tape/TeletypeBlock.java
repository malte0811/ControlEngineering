package malte0811.controlengineering.blocks.tape;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalPlacement;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.blocks.shapes.HorizontalShapeProvider;
import malte0811.controlengineering.gui.TeletypeScreen;
import malte0811.controlengineering.tiles.tape.TeletypeTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeletypeBlock extends CEBlock<Direction> {
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape BASE_SHAPE = VoxelShapes.or(
            makeCuboidShape(0, 0, 0, 16, 10, 8),
            makeCuboidShape(0, 0, 8, 16, 4, 16)
    );

    public TeletypeBlock() {
        super(
                Properties.create(Material.IRON).notSolid(),
                new HorizontalPlacement(FACING),
                new HorizontalShapeProvider(FromBlockFunction.getProperty(FACING), BASE_SHAPE)
        );
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TeletypeTile();
    }

    @Override
    public ActionResultType onBlockActivated(
            BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit
    ) {
        if (worldIn.isRemote) {
            Minecraft.getInstance().displayGuiScreen(new TeletypeScreen());
        }
        return ActionResultType.SUCCESS;
    }
}
