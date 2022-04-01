package malte0811.controlengineering.controlpanels;

import com.mojang.math.Matrix4f;
import malte0811.controlengineering.blockentity.panels.ControlPanelBlockEntity;
import malte0811.controlengineering.blocks.panels.CachedPanelShape;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.util.BEUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public class PanelSelectionShapes extends SelectionShapes {
    private final ControlPanelBlockEntity bEntity;

    public PanelSelectionShapes(ControlPanelBlockEntity bEntity) {
        this.bEntity = bEntity;
    }

    @Override
    @Nullable
    public VoxelShape mainShape() {
        return CachedPanelShape.getPanelShape(bEntity.getTransform());
    }

    @Nonnull
    @Override
    public Matrix4f outerToInnerPosition() {
        return bEntity.getTransform().getWorldToPanelTop();
    }

    @Nonnull
    @Override
    public List<? extends SelectionShapes> innerShapes() {
        return bEntity.getComponents();
    }

    @Override
    public void plotBox(BiConsumer<Vec3, Vec3> drawLine) {
        PanelTransform transform = bEntity.getTransform();
        Vec3[] bottomVertices = transform.getBottomVertices();
        Vec3[] topVertices = transform.getTopVertices();
        for (int i = 0; i < 4; ++i) {
            drawLine.accept(bottomVertices[i], bottomVertices[(i + 1) % 4]);
            drawLine.accept(topVertices[i], topVertices[(i + 1) % 4]);
            drawLine.accept(bottomVertices[i], topVertices[i]);
        }
    }

    @Override
    public InteractionResult onUse(UseOnContext ctx, InteractionResult defaultType, Vec3 relativeHit) {
        if (defaultType.shouldSwing() && !bEntity.getLevel().isClientSide) {
            bEntity.updateBusState();
            bEntity.setChanged();
            BEUtil.markDirtyAndSync(bEntity);
        }
        return defaultType;
    }

    @Override
    public boolean shouldRenderNonTop() {
        return true;
    }

    @Override
    public boolean allowTargetThroughOuter() {
        // TODO make more flexible by allowing the actual hit position to be computed
        return true;
    }
}
