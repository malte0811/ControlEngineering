package malte0811.controlengineering.blocks.panels;

import blusunrize.immersiveengineering.api.IEProperties;
import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalStructurePlacement;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.blocks.shapes.HorizontalWithExtraShape;
import malte0811.controlengineering.blocks.tape.TeletypeBlock;
import malte0811.controlengineering.gui.panel.PanelLayoutContainer;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.panels.PanelDesignerTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class PanelDesignerBlock extends CEBlock<Direction, PanelDesignerTile> {
    public static final Property<Offset> OFFSET = EnumProperty.create("offset", Offset.class);
    public static final Property<Direction> FACING = IEProperties.FACING_HORIZONTAL;
    public static final VoxelShape TABLE_TOP = createPixelRelative(0, 13, 0, 16, 16, 16);
    private static final VoxelShape TABLE_FRONT_SHAPE = VoxelShapes.or(
            TABLE_TOP,
            createPixelRelative(1, 0, 12, 4, 13, 15),
            createPixelRelative(12, 0, 12, 15, 13, 15)
    );
    private static final VoxelShape TABLE_BACK_SHAPE = VoxelShapes.or(
            TABLE_TOP,
            createPixelRelative(1, 0, 1, 4, 13, 4),
            createPixelRelative(12, 0, 1, 15, 13, 4)
    );
    private static final VoxelShape TOP_FRONT_SHAPE = VoxelShapes.or(
            createPixelRelative(0, 0, 0, 16, 16, 1),
            createPixelRelative(0, 8, 0, 16, 16, 12)
    );
    public static final VoxelShape TTY_BASE_SHAPE = TeletypeBlock.SHAPE_PROVIDER.apply(Direction.NORTH);
    private static final VoxelShape TOP_BACK_SHAPE = VoxelShapes.or(
            TTY_BASE_SHAPE,
            createPixelRelative(0, 8, 8, 16, 16, 16),
            createPixelRelative(1, 4, 8, 15, 8, 16)
    );
    public static final HorizontalWithExtraShape<Offset> SHAPE = new HorizontalWithExtraShape<>(
            FromBlockFunction.getProperty(OFFSET),
            FromBlockFunction.getProperty(FACING),
            ImmutableMap.of(
                    Offset.ORIGIN, TABLE_FRONT_SHAPE,
                    Offset.BACK, TABLE_BACK_SHAPE,
                    Offset.FRONT_TOP, TOP_FRONT_SHAPE,
                    Offset.BACK_TOP, TOP_BACK_SHAPE
            )
    );
    public static final VoxelShape BUTTON = createPixelRelative(0, 10, 10, 1, 14, 14);

    public PanelDesignerBlock() {
        super(
                defaultPropertiesNotSolid(),
                new HorizontalStructurePlacement<>(FACING, OFFSET, Offset::getOffset),
                SHAPE,
                CETileEntities.PANEL_DESIGNER
        );
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(OFFSET, FACING);
    }

    @Nullable
    @Override
    public INamedContainerProvider getContainer(
            @Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos
    ) {
        return new SimpleNamedContainerProvider(
                (id, inv, player) -> new PanelLayoutContainer(IWorldPosCallable.of(worldIn, pos), id),
                StringTextComponent.EMPTY
        );
    }

    public enum Offset implements IStringSerializable {
        ORIGIN(0, 0),
        BACK(0, -1),
        FRONT_TOP(1, 0),
        BACK_TOP(1, -1),
        ;

        private final int zOff;
        private final int yOff;

        Offset(int yOff, int zOff) {
            this.zOff = zOff;
            this.yOff = yOff;
        }

        public BlockPos getOffset() {
            return new BlockPos(0, yOff, zOff);
        }

        @Nonnull
        @Override
        public String getString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
