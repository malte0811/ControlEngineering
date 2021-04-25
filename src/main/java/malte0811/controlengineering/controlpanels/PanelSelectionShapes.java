package malte0811.controlengineering.controlpanels;

import malte0811.controlengineering.blocks.panels.CachedPanelShape;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import malte0811.controlengineering.util.math.Matrix4;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public class PanelSelectionShapes extends SelectionShapes {
    private final ControlPanelTile tile;

    public PanelSelectionShapes(ControlPanelTile tile) {
        this.tile = tile;
    }

    @Override
    @Nullable
    public VoxelShape mainShape() {
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
    public void plotBox(BiConsumer<Vector3d, Vector3d> drawLine) {
        PanelTransform transform = tile.getTransform();
        Vector3d[] bottomVertices = transform.getBottomVertices();
        Vector3d[] topVertices = transform.getTopVertices();
        for (int i = 0; i < 4; ++i) {
            drawLine.accept(bottomVertices[i], bottomVertices[(i + 1) % 4]);
            drawLine.accept(topVertices[i], topVertices[(i + 1) % 4]);
            drawLine.accept(bottomVertices[i], topVertices[i]);
        }
    }

    @Override
    public ActionResultType onUse(ItemUseContext ctx, ActionResultType defaultType) {
        if (defaultType.isSuccess() && !tile.getWorld().isRemote) {
            tile.updateBusState(ControlPanelTile.SyncType.ALWAYS);
            tile.markDirty();
        }
        return defaultType;
    }

    @Override
    public boolean shouldRenderNonTop() {
        return true;
    }
}
