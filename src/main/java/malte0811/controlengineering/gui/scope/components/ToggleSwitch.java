package malte0811.controlengineering.gui.scope.components;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static malte0811.controlengineering.gui.scope.ScopeScreen.TEXTURE;

public class ToggleSwitch implements IScopeComponent {
    private static final SubTexture HIGH = new SubTexture(TEXTURE, 241, 245, 246, 256);
    private static final SubTexture LOW = new SubTexture(TEXTURE, 246, 245, 251, 256);
    private static final SubTexture NEUTRAL = new SubTexture(TEXTURE, 251, 245, 256, 256);

    private final Component tooltip;
    private final Vec2i pos;
    private final boolean allowTristate;
    private final Consumer<State> setState;
    private final State state;
    private final RectangleI area;

    public ToggleSwitch(
            Component tooltip, Vec2i pos, boolean allowTristate, State state, Consumer<State> setState
    ) {
        this.tooltip = tooltip;
        this.allowTristate = allowTristate;
        this.pos = pos;
        this.state = state;
        this.setState = setState;
        this.area = new RectangleI(pos, pos.add(5, 11));
    }

    public ToggleSwitch(Component tooltip, Vec2i pos, boolean state, BooleanConsumer setState) {
        this(tooltip, pos, false, state ? State.HIGH : State.LOW, newState -> setState.accept(newState == State.HIGH));
    }

    @Override
    public void render(PoseStack transform) {
        final var texture = switch (this.state) {
            case HIGH -> HIGH;
            case NEUTRAL -> NEUTRAL;
            case LOW -> LOW;
        };
        texture.blit(transform, pos.x(), pos.y());
    }

    @Override
    public boolean click(double x, double y) {
        final var yRel = y - pos.y();
        final State newValue;
        if (allowTristate && yRel >= 3 && yRel <= 8) {
            newValue = State.NEUTRAL;
        } else if (yRel > 5.5) {
            newValue = State.LOW;
        } else {
            newValue = State.HIGH;
        }
        if (state == newValue) { return false; }
        setState.accept(newValue);
        return true;
    }

    @Override
    public RectangleI getArea() {
        return area;
    }

    @Override
    public Component getTooltip() {
        return tooltip;
    }

    public enum State {
        HIGH, NEUTRAL, LOW;
    }
}
