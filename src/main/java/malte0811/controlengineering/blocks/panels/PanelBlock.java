package malte0811.controlengineering.blocks.panels;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import javax.annotation.Nonnull;

public class PanelBlock extends CEBlock<PanelOrientation, ControlPanelTile> {
    public static final BooleanProperty IS_BASE = BooleanProperty.create("base");

    public PanelBlock() {
        super(
                defaultPropertiesNotSolid(),
                new PanelPlacementBehavior(),
                CachedPanelShape.create(),
                CETileEntities.CONTROL_PANEL
        );
    }

    public static ControlPanelTile getBase(BlockGetter world, BlockState state, BlockPos pos) {
        BlockPos masterPos;
        if (state.getValue(IS_BASE)) {
            masterPos = pos;
        } else {
            PanelOrientation po = state.getValue(PanelOrientation.PROPERTY);
            masterPos = pos.relative(po.top, -1);
        }
        BlockEntity te = world.getBlockEntity(masterPos);
        if (te instanceof ControlPanelTile) {
            return (ControlPanelTile) te;
        } else {
            return null;
        }
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IS_BASE, PanelOrientation.PROPERTY);
    }
}
