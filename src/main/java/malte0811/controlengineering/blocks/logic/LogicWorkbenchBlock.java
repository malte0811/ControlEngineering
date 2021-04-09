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
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class LogicWorkbenchBlock extends CEBlock<Direction, LogicWorkbenchTile> {
    public static final Property<Offset> OFFSET = EnumProperty.create("offset", Offset.class);
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final HorizontalWithExtraShape<Offset> SHAPE = new HorizontalWithExtraShape<>(
            FromBlockFunction.getProperty(OFFSET),
            FromBlockFunction.getProperty(FACING),
            ImmutableMap.of(
                    Offset.ORIGIN, VoxelShapes.fullCube(),
                    Offset.BACK_LEFT, VoxelShapes.fullCube(),
                    Offset.FRONT_RIGHT, VoxelShapes.fullCube(),
                    Offset.OPPOSITE, VoxelShapes.fullCube()
            )
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
        OPPOSITE(1, -1);
        private final BlockPos offset;

        Offset(int xOff, int zOff) {
            this.offset = new BlockPos(xOff, 0, zOff);
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
