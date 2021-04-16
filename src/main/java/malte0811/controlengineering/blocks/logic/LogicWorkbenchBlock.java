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
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class LogicWorkbenchBlock extends CEBlock<Direction, LogicWorkbenchTile> {
    public static final Property<Offset> OFFSET = EnumProperty.create("offset", Offset.class);
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape TABLE_TOP = VoxelShapes.create(0, 13 / 16., 0, 1, 1, 1);
    private static final VoxelShape LEG_LEFT_FRONT = VoxelShapes.create(1 / 16., 0, 12 / 16., 4 / 16., 1, 15 / 16.);
    private static final VoxelShape LEG_LEFT_BACK = VoxelShapes.create(1 / 16., 0, 1 / 16., 4 / 16., 1, 4 / 16.);
    private static final VoxelShape LEG_RIGHT_FRONT = VoxelShapes.create(12 / 16., 0, 12 / 16., 15 / 16., 1, 15 / 16.);
    private static final VoxelShape LEG_RIGHT_BACK = VoxelShapes.create(12 / 16., 0, 1 / 16., 15 / 16., 1, 4 / 16.);
    private static final VoxelShape DRAWER_LOW = VoxelShapes.create(
            1 / 16., 7 / 16., 1 / 16., 15 / 16., 13 / 16., 15 / 16.
    );
    public static final VoxelShape BURNER = VoxelShapes.or(
            VoxelShapes.create(11 / 16., 0, 3 / 16., 13 / 16., 6 / 16., 5 / 16.),
            VoxelShapes.create(9 / 16., 9 / 32., 7 / 32., 11 / 16., 11 / 32., 9 / 32.)
    );
    private static final VoxelShape DRAWERS_TOP = VoxelShapes.or(
            VoxelShapes.create(2 / 16., 0, 2 / 16., 10 / 16., 8 / 16., 8 / 16.),
            VoxelShapes.create(12 / 16., 0, 2 / 16., 20 / 16., 8 / 16., 8 / 16.)
    );
    public static final VoxelShape DRAWERS_TOP_RIGHT = DRAWERS_TOP.withOffset(-1, 0, 0);

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
                    .put(Offset.TOP_RIGHT, VoxelShapes.or(BURNER, DRAWERS_TOP_RIGHT))
                    .build()
    );

    public LogicWorkbenchBlock() {
        super(
                Properties.create(Material.IRON).notSolid().setOpaque(($1, $2, $3) -> false),
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
        return new SimpleNamedContainerProvider(
                (id, inv, player) -> new LogicDesignContainer(id, IWorldPosCallable.of(worldIn, pos)),
                new TranslationTextComponent("screen.controlengineering.logic_design")
        );
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
