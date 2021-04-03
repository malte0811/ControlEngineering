package malte0811.controlengineering.blocks.tape;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalPlacement;
import malte0811.controlengineering.blocks.shapes.CachedShape;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.blocks.shapes.HorizontalShapeProvider;
import malte0811.controlengineering.gui.tape.TeletypeContainer;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.tape.TeletypeTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeletypeBlock extends CEBlock<Direction, TeletypeTile> {
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape BASE_SHAPE = VoxelShapes.or(
            makeCuboidShape(0, 0, 0, 16, 10, 8),
            makeCuboidShape(0, 0, 8, 16, 4, 16)
    );
    public static final CachedShape<Direction> SHAPE_PROVIDER = new HorizontalShapeProvider(
            FromBlockFunction.getProperty(FACING), BASE_SHAPE
    );

    public TeletypeBlock() {
        super(
                Properties.create(Material.IRON).notSolid(),
                new HorizontalPlacement(FACING),
                SHAPE_PROVIDER,
                CETileEntities.TELETYPE
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

    @Nullable
    @Override
    public INamedContainerProvider getContainer(
            @Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos
    ) {
        return new SimpleNamedContainerProvider(
                (id, inv, player) -> new TeletypeContainer(id, IWorldPosCallable.of(worldIn, pos)),
                new TranslationTextComponent("screen.controlengineering.teletype")
        );
    }
}
