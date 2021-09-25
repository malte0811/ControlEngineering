package malte0811.controlengineering.blocks.shapes;

import malte0811.controlengineering.util.math.Matrix4;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class SelectionShapes {
    private Supplier<String> getText = () -> null;

    @Nullable
    public abstract VoxelShape mainShape();

    @Nonnull
    public abstract Matrix4 outerToInnerPosition();

    @Nonnull
    public abstract List<? extends SelectionShapes> innerShapes();

    @Nullable
    public final String getOverlayText() {
        return getText.get();
    }

    public SelectionShapes setTextGetter(Supplier<String> getText) {
        this.getText = getText;
        return this;
    }

    public abstract InteractionResult onUse(UseOnContext ctx, InteractionResult defaultType);

    public void plotBox(BiConsumer<Vec3, Vec3> drawLine) {
        VoxelShape main = mainShape();
        if (main != null) {
            main.forAllEdges(
                    (x1, y1, z1, x2, y2, z2) -> drawLine.accept(new Vec3(x1, y1, z1), new Vec3(x2, y2, z2))
            );
        }
    }

    public final InteractionResult onUse(UseOnContext useCtx, ClipContext ray) {
        List<SelectionShapes> stack = getTargeted(ray);
        InteractionResult ret = InteractionResult.PASS;
        for (int i = 0; i < stack.size(); ++i) {
            ret = stack.get(stack.size() - i - 1).onUse(useCtx, ret);
        }
        return ret;
    }

    public final List<SelectionShapes> getTargeted(ClipContext ray) {
        List<SelectionShapes> result = new ArrayList<>();
        fillTargetedStack(ray, result);
        return result;
    }

    private void fillTargetedStack(ClipContext ray, List<SelectionShapes> out) {
        out.add(this);
        List<? extends SelectionShapes> innerShapes = innerShapes();
        if (innerShapes.isEmpty()) {
            return;
        }
        ClipContext innerRay = outerToInnerPosition().transformRay(ray.getFrom(), ray.getTo());
        Optional<SelectionShapes> closest = Optional.empty();
        double minDistanceSq = Double.POSITIVE_INFINITY;
        for (SelectionShapes inner : innerShapes) {
            final VoxelShape innerShape = inner.mainShape();
            if (innerShape != null) {
                final BlockHitResult result = innerShape.clip(
                        innerRay.getFrom(), innerRay.getTo(), BlockPos.ZERO
                );
                if (result != null) {
                    final double distanceSq = result.getLocation().distanceToSqr(innerRay.getFrom());
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

    public boolean shouldRenderNonTop() {
        return false;
    }
}
