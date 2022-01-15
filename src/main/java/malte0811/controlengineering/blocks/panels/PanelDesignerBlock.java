package malte0811.controlengineering.blocks.panels;

import blusunrize.immersiveengineering.api.IEProperties;
import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalStructurePlacement;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.blocks.shapes.HorizontalWithExtraShape;
import malte0811.controlengineering.blocks.tape.KeypunchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.Locale;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class PanelDesignerBlock extends CEBlock<Direction> {
    public static final Property<Offset> OFFSET = EnumProperty.create("offset", Offset.class);
    public static final Property<Direction> FACING = IEProperties.FACING_HORIZONTAL;
    public static final VoxelShape TABLE_TOP = createPixelRelative(0, 13, 0, 16, 16, 16);
    private static final VoxelShape TABLE_FRONT_SHAPE = Shapes.or(
            TABLE_TOP,
            createPixelRelative(1, 0, 12, 4, 13, 15),
            createPixelRelative(12, 0, 12, 15, 13, 15)
    );
    private static final VoxelShape TABLE_BACK_SHAPE = Shapes.block();
    private static final VoxelShape TOP_FRONT_SHAPE = Shapes.or(
            createPixelRelative(0, 0, 0, 16, 16, 1),
            createPixelRelative(0, 8, 0, 16, 16, 12)
    );
    public static final VoxelShape TTY_BASE_SHAPE = KeypunchBlock.SHAPE_PROVIDER.apply(Direction.NORTH);
    private static final VoxelShape TOP_BACK_SHAPE = Shapes.or(
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
                CEBlockEntities.PANEL_DESIGNER
        );
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OFFSET, FACING);
    }

    public static boolean isMaster(BlockState state) {
        return state.getValue(OFFSET) == Offset.ORIGIN;
    }

    public enum Offset implements StringRepresentable {
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
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
