package malte0811.controlengineering.controlpanels;

import malte0811.controlengineering.blocks.panels.CachedPanelShape;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import malte0811.controlengineering.util.math.Matrix4;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
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
    public void plotBox(BiConsumer<Vec3, Vec3> drawLine) {
        PanelTransform transform = tile.getTransform();
        Vec3[] bottomVertices = transform.getBottomVertices();
        Vec3[] topVertices = transform.getTopVertices();
        for (int i = 0; i < 4; ++i) {
            drawLine.accept(bottomVertices[i], bottomVertices[(i + 1) % 4]);
            drawLine.accept(topVertices[i], topVertices[(i + 1) % 4]);
            drawLine.accept(bottomVertices[i], topVertices[i]);
        }
    }

    @Override
    public InteractionResult onUse(UseOnContext ctx, InteractionResult defaultType) {
        if (defaultType.shouldSwing() && !tile.getLevel().isClientSide) {
            tile.updateBusState(ControlPanelTile.SyncType.ALWAYS);
            tile.setChanged();
        }
        return defaultType;
    }

    @Override
    public boolean shouldRenderNonTop() {
        return true;
    }
}
