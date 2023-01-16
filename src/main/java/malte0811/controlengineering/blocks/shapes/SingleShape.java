package malte0811.controlengineering.blocks.shapes;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4fc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class SingleShape extends SelectionShapes {
    public static final SelectionShapes FULL_BLOCK = new SingleShape(Shapes.block(), $ -> InteractionResult.PASS);

    private final VoxelShape mainShape;
    private final Function<UseOnContext, InteractionResult> onClick;

    public SingleShape(VoxelShape mainShape, Function<UseOnContext, InteractionResult> onClick) {
        this.mainShape = mainShape;
        this.onClick = onClick;
    }

    @Override
    public @Nullable VoxelShape mainShape() {
        return this.mainShape;
    }

    @Nonnull
    @Override
    public Matrix4fc outerToInnerPosition() {
        return IDENTITY;
    }

    @Nonnull
    @Override
    public List<? extends SelectionShapes> innerShapes() {
        return ImmutableList.of();
    }

    @Override
    public InteractionResult onUse(UseOnContext ctx, InteractionResult defaultType, Vec3 relativeHit) {
        return onClick.apply(ctx);
    }
}
