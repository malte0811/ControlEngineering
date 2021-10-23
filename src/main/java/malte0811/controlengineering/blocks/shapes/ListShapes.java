package malte0811.controlengineering.blocks.shapes;

import com.mojang.math.Matrix4f;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class ListShapes extends SelectionShapes {
    private final VoxelShape mainShape;
    private final Matrix4f outerToInner;
    private final List<? extends SelectionShapes> shapes;
    private final Function<UseOnContext, InteractionResult> onClick;

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
    public Matrix4f outerToInnerPosition() {
        return outerToInner;
    }

    @Nonnull
    @Override
    public List<? extends SelectionShapes> innerShapes() {
        return shapes;
    }

    @Override
    public InteractionResult onUse(UseOnContext ctx, InteractionResult defaultType) {
        if (defaultType == InteractionResult.PASS) {
            return onClick.apply(ctx);
        } else {
            return defaultType;
        }
    }
}
