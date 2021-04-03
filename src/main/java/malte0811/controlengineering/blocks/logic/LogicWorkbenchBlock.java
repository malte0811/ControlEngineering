package malte0811.controlengineering.blocks.logic;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalStructurePlacement;
import malte0811.controlengineering.gui.logic.LogicDesignScreen;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.logic.LogicWorkbenchTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Locale;

public class LogicWorkbenchBlock extends CEBlock<Direction, LogicWorkbenchTile> {
    public static final Property<Offset> OFFSET = EnumProperty.create("offset", Offset.class);
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public LogicWorkbenchBlock() {
        super(
                Properties.create(Material.IRON).notSolid().setOpaque(($1, $2, $3) -> false),
                new HorizontalStructurePlacement<>(FACING, OFFSET, Offset::getOffset),
                (state, world, pos) -> VoxelShapes.fullCube(),
                CETileEntities.LOGIC_WORKBENCH
        );
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(OFFSET, FACING);
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
        //TODO
        if (worldIn.isRemote) {
            Minecraft.getInstance().displayGuiScreen(new LogicDesignScreen());
        }
        return ActionResultType.SUCCESS;
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

        @Override
        public String getString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
