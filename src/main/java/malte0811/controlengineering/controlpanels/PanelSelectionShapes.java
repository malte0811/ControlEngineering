package malte0811.controlengineering.controlpanels;

import malte0811.controlengineering.blocks.panels.CachedPanelShape;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import malte0811.controlengineering.util.Matrix4;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PanelSelectionShapes extends SelectionShapes {
    private final ControlPanelTile tile;

    public PanelSelectionShapes(ControlPanelTile tile) {
        this.tile = tile;
    }

    @Override
    public @Nullable
    VoxelShape mainShape() {
        return CachedPanelShape.getPanelShape(tile.getTransform());
    }

    @Nonnull
    @Override
    public Matrix4 outerToInnerPosition() {
        return tile.getTransform().getWorldToPanelTop();
    }

    @Nonnull
    @Override
    public List<? extends SelectionShapes> innerShapes() {
        return tile.getComponents();
    }

    @Override
    public ActionResultType onUse(ItemUseContext ctx, ActionResultType defaultType) {
        if (defaultType.isSuccess() && !tile.getWorld().isRemote) {
            tile.updateBusState(ControlPanelTile.SyncType.ALWAYS);
            tile.markDirty();
        }
        return defaultType;
    }
}
