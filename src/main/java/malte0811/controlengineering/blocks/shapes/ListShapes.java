package malte0811.controlengineering.blocks.shapes;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class ListShapes extends SelectionShapes {
    private final VoxelShape mainShape;
    private final Matrix4f outerToInner;
    private final List<? extends SelectionShapes> shapes;
    private final Function<UseOnContext, InteractionResult> onClick;
    private boolean allowTargetThrough = false;

    public ListShapes(
            VoxelShape mainShape,
            Matrix4f outerToInner,
            List<? extends SelectionShapes> shapes,
            Function<UseOnContext, InteractionResult> onClick
    ) {
        this.mainShape = mainShape;
        this.outerToInner = outerToInner;
        this.shapes = shapes;
        this.onClick = onClick;
    }

    @Nullable
    @Override
    public VoxelShape mainShape() {
        return mainShape;
    }

    @Nonnull
    @Override
    public Matrix4fc outerToInnerPosition() {
        return outerToInner;
    }

    @Nonnull
    @Override
    public List<? extends SelectionShapes> innerShapes() {
        return shapes;
    }

    @Override
    public InteractionResult onUse(UseOnContext ctx, InteractionResult defaultType, Vec3 relativeHit) {
        if (defaultType == InteractionResult.PASS) {
            return onClick.apply(ctx);
        } else {
            return defaultType;
        }
    }

    public SelectionShapes setAllowTargetThrough(boolean b) {
        this.allowTargetThrough = b;
        return this;
    }

    @Override
    public boolean allowTargetThroughOuter() {
        return this.allowTargetThrough;
    }
}
