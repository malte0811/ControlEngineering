package malte0811.controlengineering.controlpanels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.util.math.vector.Vector2f;

public class PlacedComponent {
    public static final Codec<PlacedComponent> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    PanelComponent.CODEC.fieldOf("component").forGetter(pc -> pc.component),
                    Vec2d.CODEC.fieldOf("position").forGetter(pc -> pc.pos)
            ).apply(inst, PlacedComponent::new)
    );

    private final PanelComponent<?> component;
    private final Vec2d pos;

    public PlacedComponent(PanelComponent<?> component, Vec2d pos) {
        this.component = component;
        this.pos = pos;
    }

    public PanelComponent<?> getComponent() {
        return component;
    }

    public Vec2d getPos() {
        return pos;
    }
}
