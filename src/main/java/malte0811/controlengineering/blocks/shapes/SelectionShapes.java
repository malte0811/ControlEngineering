package malte0811.controlengineering.blocks.shapes;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.util.math.MatrixUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class SelectionShapes {
    private Supplier<Component> getText = () -> null;
    protected static final Matrix4fc IDENTITY = new Matrix4f();

    @Nullable
    public abstract VoxelShape mainShape();

    @Nonnull
    public abstract Matrix4fc outerToInnerPosition();

    @Nonnull
    public abstract List<? extends SelectionShapes> innerShapes();

    @Nullable
    public final Component getOverlayText() {
        return getText.get();
    }

    public SelectionShapes setTextGetter(Supplier<Component> getText) {
        this.getText = getText;
        return this;
    }

    public abstract InteractionResult onUse(UseOnContext ctx, InteractionResult defaultType, Vec3 relativeHit);

    public void plotBox(BiConsumer<Vec3, Vec3> drawLine) {
        VoxelShape main = mainShape();
        if (main != null) {
            main.forAllEdges(
                    (x1, y1, z1, x2, y2, z2) -> drawLine.accept(new Vec3(x1, y1, z1), new Vec3(x2, y2, z2))
            );
        }
    }

    public final InteractionResult onUse(UseOnContext useCtx, ClipContext ray) {
        Pair<List<SelectionShapes>, Vec3> targeted = getTargeted(ray);
        var stack = targeted.getFirst();
        InteractionResult ret = InteractionResult.PASS;
        for (int i = 0; i < stack.size(); ++i) {
            ret = stack.get(stack.size() - i - 1).onUse(useCtx, ret, targeted.getSecond());
        }
        return ret;
    }

    public final Pair<List<SelectionShapes>, Vec3> getTargeted(ClipContext ray) {
        double maxDistanceForInner = Double.POSITIVE_INFINITY;
        var mainShape = mainShape();
        if (mainShape != null) {
            final BlockHitResult result = mainShape.clip(ray.getFrom(), ray.getTo(), BlockPos.ZERO);
            if (result != null) {
                maxDistanceForInner = result.getLocation().distanceToSqr(ray.getFrom());
            }
        }
        List<SelectionShapes> stack = new ArrayList<>();
        Vec3 innermostHit = fillTargetedStack(ray, maxDistanceForInner, stack);
        return Pair.of(stack, innermostHit);
    }

    private Vec3 fillTargetedStack(ClipContext ray, double limitDistanceSq, List<SelectionShapes> out) {
        out.add(this);
        List<? extends SelectionShapes> innerShapes = innerShapes();
        if (innerShapes.isEmpty()) {
            return null;
        }
        ClipContext innerRay = MatrixUtils.transformRay(outerToInnerPosition(), ray.getFrom(), ray.getTo());
        SelectionShapes closest = null;
        double minDistanceSq = allowTargetThroughOuter() ? Double.POSITIVE_INFINITY : (limitDistanceSq + 1e-3);
        Vec3 closestHit = null;
        for (SelectionShapes inner : innerShapes) {
            final VoxelShape innerShape = inner.mainShape();
            if (innerShape != null) {
                final BlockHitResult result = innerShape.clip(innerRay.getFrom(), innerRay.getTo(), BlockPos.ZERO);
                if (result != null) {
                    final double distanceSq = result.getLocation().distanceToSqr(innerRay.getFrom());
                    if (distanceSq < minDistanceSq) {
                        minDistanceSq = distanceSq;
                        closest = inner;
                        closestHit = result.getLocation();
                    }
                }
            }
        }
        if (closest != null) {
            var innerHit = closest.fillTargetedStack(innerRay, minDistanceSq, out);
            if (innerHit != null)
                return innerHit;
        }
        return closestHit;
    }

    public boolean shouldRenderNonTop() {
        return false;
    }

    public boolean allowTargetThroughOuter() {
        return false;
    }
}
