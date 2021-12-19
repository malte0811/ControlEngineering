package malte0811.controlengineering.blocks.panels;

import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blockentity.panels.ControlPanelBlockEntity;
import malte0811.controlengineering.blocks.CEBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nonnull;

public class PanelBlock extends CEBlock<PanelOrientation> {
    public static final BooleanProperty IS_BASE = BooleanProperty.create("base");

    public PanelBlock() {
        super(
                defaultPropertiesNotSolid(),
                new PanelPlacementBehavior(),
                CachedPanelShape.create(),
                CEBlockEntities.CONTROL_PANEL
        );
    }

    public static ControlPanelBlockEntity getBase(BlockGetter world, BlockState state, BlockPos pos) {
        BlockPos masterPos;
        if (isMaster(state)) {
            masterPos = pos;
        } else {
            PanelOrientation po = state.getValue(PanelOrientation.PROPERTY);
            masterPos = pos.relative(po.top, -1);
        }
        return world.getBlockEntity(masterPos) instanceof ControlPanelBlockEntity panel ? panel : null;
    }

    public static boolean isMaster(BlockState state) {
        return state.getValue(IS_BASE);
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IS_BASE, PanelOrientation.PROPERTY);
    }
}
