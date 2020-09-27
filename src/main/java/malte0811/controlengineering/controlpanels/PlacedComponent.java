package malte0811.controlengineering.controlpanels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraftforge.common.util.Lazy;

public class PlacedComponent {
    public static final Codec<PlacedComponent> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    PanelComponent.CODEC.fieldOf("component").forGetter(pc -> pc.component),
                    Vec2d.CODEC.fieldOf("position").forGetter(pc -> pc.pos)
            ).apply(inst, PlacedComponent::new)
    );

    private final PanelComponent<?> component;
    private final Vec2d pos;
    private final Lazy<AxisAlignedBB> shape;

    public PlacedComponent(PanelComponent<?> component, Vec2d pos) {
        this.component = component;
        this.pos = pos;
        shape = Lazy.of(() -> {
            AxisAlignedBB compShape = component.getSelectionBox();
            if (compShape == null) {
                return null;
            } else {
                return scale(compShape.offset(pos.x, 0, pos.y), 1/16d);
            }
        });
    }

    public PanelComponent<?> getComponent() {
        return component;
    }

    public Vec2d getPos() {
        return pos;
    }

    public AxisAlignedBB getSelectionShape() {
        return shape.get();
    }

    private static AxisAlignedBB scale(AxisAlignedBB in, double scale) {
        return new AxisAlignedBB(
                in.minX * scale,
                in.minY * scale,
                in.minZ * scale,
                in.maxX * scale,
                in.maxY * scale,
                in.maxZ * scale
        );
    }
}
