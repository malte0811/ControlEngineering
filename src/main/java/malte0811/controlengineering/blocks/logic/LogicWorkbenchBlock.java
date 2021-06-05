package malte0811.controlengineering.blocks.logic;

import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalStructurePlacement;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.blocks.shapes.HorizontalWithExtraShape;
import malte0811.controlengineering.gui.logic.LogicDesignContainer;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.logic.LogicWorkbenchTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class LogicWorkbenchBlock extends CEBlock<Direction, LogicWorkbenchTile> {
    public static final Property<Offset> OFFSET = EnumProperty.create("offset", Offset.class);
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape TABLE_TOP = createPixelRelative(0, 13, 0, 16, 16, 16);
    private static final VoxelShape LEG_LEFT_FRONT = createPixelRelative(1, 0, 12, 4, 16, 15);
    private static final VoxelShape LEG_LEFT_BACK = createPixelRelative(1, 0, 1, 4, 16, 4);
    private static final VoxelShape LEG_RIGHT_FRONT = createPixelRelative(12, 0, 12, 15, 16, 15);
    private static final VoxelShape LEG_RIGHT_BACK = createPixelRelative(12, 0, 1, 15, 16, 4);
    private static final VoxelShape DRAWER_LOW = createPixelRelative(1, 7, 1, 15, 13, 15);
    public static final VoxelShape BURNER = VoxelShapes.or(
            createPixelRelative(11, 0, 3, 13, 6, 5),
            createPixelRelative(9, 4.5, 3.5, 11, 5.5, 4.5)
    );
    public static final VoxelShape TUBE_DRAWER = createPixelRelative(2, 0, 2, 10, 8, 8);
    public static final VoxelShape WIRE_DRAWER = createPixelRelative(12, 0, 2, 20, 8, 8);
    private static final VoxelShape DRAWERS_TOP = VoxelShapes.or(TUBE_DRAWER, WIRE_DRAWER);
    public static final VoxelShape WIRE_DRAWER_TOP_RIGHT = WIRE_DRAWER.withOffset(-1, 0, 0);

    public static final HorizontalWithExtraShape<Offset> SHAPE = new HorizontalWithExtraShape<>(
            FromBlockFunction.getProperty(OFFSET),
            FromBlockFunction.getProperty(FACING),
            ImmutableMap.<Offset, VoxelShape>builder()
                    .put(Offset.ORIGIN, VoxelShapes.or(
                            TABLE_TOP,
                            LEG_LEFT_FRONT,
                            LEG_LEFT_BACK,
                            LEG_RIGHT_FRONT,
                            LEG_RIGHT_BACK,
                            DRAWER_LOW
                    ))
                    .put(Offset.BACK_LEFT, VoxelShapes.or(TABLE_TOP, LEG_LEFT_BACK))
                    .put(Offset.FRONT_RIGHT, VoxelShapes.or(TABLE_TOP, LEG_RIGHT_FRONT))
                    .put(Offset.BACK_RIGHT, VoxelShapes.or(TABLE_TOP, LEG_RIGHT_BACK))
                    .put(Offset.TOP_LEFT, DRAWERS_TOP)
                    .put(Offset.TOP_RIGHT, VoxelShapes.or(BURNER, WIRE_DRAWER_TOP_RIGHT))
                    .build()
    );

    public LogicWorkbenchBlock() {
        super(
                defaultPropertiesNotSolid(),
                new HorizontalStructurePlacement<>(FACING, OFFSET, Offset::getOffset),
                SHAPE,
                CETileEntities.LOGIC_WORKBENCH
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
        return LogicDesignContainer.makeProvider(worldIn, pos, false);
    }

    public enum Offset implements IStringSerializable {
        ORIGIN(0, 0),
        FRONT_RIGHT(1, 0),
        BACK_LEFT(0, -1),
        BACK_RIGHT(1, -1),
        TOP_LEFT(0, 1, -1),
        TOP_RIGHT(1, 1, -1);
        private final BlockPos offset;

        Offset(int xOff, int zOff) {
            this(xOff, 0, zOff);
        }

        Offset(int xOff, int yOff, int zOff) {
            this.offset = new BlockPos(xOff, yOff, zOff);
        }

        public BlockPos getOffset() {
            return offset;
        }

        @Nonnull
        @Override
        public String getString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
