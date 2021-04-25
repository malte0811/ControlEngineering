package malte0811.controlengineering.blocks.logic;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.placement.HorizontalStructurePlacement;
import malte0811.controlengineering.blocks.shapes.DirectionalShapeProvider;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.logic.LogicCabinetTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShapes;

import javax.annotation.Nonnull;

public class LogicCabinetBlock extends CEBlock<Direction, LogicCabinetTile> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", 0, 1);

    public static DirectionalShapeProvider BOTTOM_SHAPE = new DirectionalShapeProvider(
            FromBlockFunction.getProperty(FACING),
            VoxelShapes.or(
                    VoxelShapes.create(0, 0, 0, 1, 0.6875, 1),
                    VoxelShapes.create(0, 0.6875, 0, 1, 1, 1 / 16.),
                    VoxelShapes.create(0, 0.6875, 0, 1 / 16., 1, 1),
                    VoxelShapes.create(15 / 16., 0.6875, 0, 1, 1, 1)
            )
    );
    public static DirectionalShapeProvider TOP_SHAPE = new DirectionalShapeProvider(
            FromBlockFunction.getProperty(FACING),
            VoxelShapes.or(
                    VoxelShapes.create(0, 0, 0, 1, 15 / 16., 1 / 16.),
                    VoxelShapes.create(0, 0, 0, 1 / 16., 15 / 16., 1),
                    VoxelShapes.create(15 / 16., 0, 0, 1, 15 / 16., 1),
                    VoxelShapes.create(0, 15 / 16., 0, 1, 1, 1)
            )
    );

    public LogicCabinetBlock() {
        super(
                Properties.create(Material.IRON).notSolid().setOpaque(($1, $2, $3) -> false),
                HorizontalStructurePlacement.column(FACING, HEIGHT),
                FromBlockFunction.either((state, world, pos) -> state.get(HEIGHT) > 0, BOTTOM_SHAPE, TOP_SHAPE),
                CETileEntities.LOGIC_CABINET
        );
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING, HEIGHT);
    }
}
