package malte0811.controlengineering.blocks.shapes;

import malte0811.controlengineering.util.Matrix4;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class SelectionShapes {
    @Nullable
    public abstract VoxelShape mainShape();

    @Nonnull
    public abstract Matrix4 outerToInnerPosition();

    @Nonnull
    public abstract List<? extends SelectionShapes> innerShapes();

    public abstract ActionResultType onUse(ItemUseContext ctx, ActionResultType defaultType);

    public void plotBox(BiConsumer<Vector3d, Vector3d> drawLine) {
        VoxelShape main = mainShape();
        if (main != null) {
            main.forEachEdge(
                    (x1, y1, z1, x2, y2, z2) -> drawLine.accept(new Vector3d(x1, y1, z1), new Vector3d(x2, y2, z2))
            );
        }
    }

    public final ActionResultType onUse(ItemUseContext useCtx, RayTraceContext ray) {
        List<SelectionShapes> stack = getTargeted(ray);
        ActionResultType ret = ActionResultType.PASS;
        for (int i = 0; i < stack.size(); ++i) {
            ret = stack.get(stack.size() - i - 1).onUse(useCtx, ret);
        }
        return ret;
    }

    public final List<SelectionShapes> getTargeted(RayTraceContext ray) {
        List<SelectionShapes> result = new ArrayList<>();
        fillTargetedStack(ray, result);
        return result;
    }

    private void fillTargetedStack(RayTraceContext ray, List<SelectionShapes> out) {
        out.add(this);
        List<? extends SelectionShapes> innerShapes = innerShapes();
        if (innerShapes.isEmpty()) {
            return;
        }
        RayTraceContext innerRay = outerToInnerPosition().transformRay(ray.getStartVec(), ray.getEndVec());
        Optional<SelectionShapes> closest = Optional.empty();
        double minDistanceSq = Double.POSITIVE_INFINITY;
        for (SelectionShapes inner : innerShapes) {
            final VoxelShape innerShape = inner.mainShape();
            if (innerShape != null) {
                final BlockRayTraceResult result = innerShape.rayTrace(
                        innerRay.getStartVec(), innerRay.getEndVec(), BlockPos.ZERO
                );
                if (result != null) {
                    final double distanceSq = result.getHitVec().squareDistanceTo(innerRay.getStartVec());
                    if (distanceSq < minDistanceSq) {
                        minDistanceSq = distanceSq;
                        closest = Optional.of(inner);
                    }
                }
            }
        }
        if (closest.isPresent()) {
            closest.get().fillTargetedStack(innerRay, out);
        }
    }
}
