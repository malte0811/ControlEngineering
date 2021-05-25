package malte0811.controlengineering.blocks.panels;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

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

    public static ControlPanelTile getBase(IBlockReader world, BlockState state, BlockPos pos) {
        BlockPos masterPos;
        if (state.get(IS_BASE)) {
            masterPos = pos;
        } else {
            PanelOrientation po = state.get(PanelOrientation.PROPERTY);
            masterPos = pos.offset(po.top, -1);
        }
        TileEntity te = world.getTileEntity(masterPos);
        if (te instanceof ControlPanelTile) {
            return (ControlPanelTile) te;
        } else {
            return null;
        }
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(IS_BASE, PanelOrientation.PROPERTY);
    }
}
