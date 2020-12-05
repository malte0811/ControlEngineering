package malte0811.controlengineering.controlpanels.components;

import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponent;
import malte0811.controlengineering.util.ReflectionUtils;
import malte0811.controlengineering.util.Vec2d;
import malte0811.controlengineering.util.serialization.StringSerializableCodecs;
import malte0811.controlengineering.util.serialization.StringSerializableField;
import malte0811.controlengineering.util.serialization.StringSerializer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.Objects;

public class Button extends PanelComponent<Button> {
    public int color;
    public boolean active;
    private BusSignalRef outputSignal;

    public Button(int color, boolean active, BusSignalRef outputSignal) {
        super(new Vec2d(1, 1));
        this.color = color;
        this.active = active;
        this.outputSignal = outputSignal;
    }

    public Button() {
        this(-1, false, new BusSignalRef(0, 0));
    }

    @Override
    public BusState getEmittedState() {
        if (active) {
            return outputSignal.singleSignalState(BusLine.MAX_VALID_VALUE);
        } else {
            return new BusState(1);
        }
    }

    @Override
    public void updateTotalState(BusState state) {}

    public static StringSerializer<Button> createCodec() {
        return new StringSerializer<>(
                ReflectionUtils.findConstructor(Button.class, int.class, boolean.class, BusSignalRef.class),
                new StringSerializableField<>("color", StringSerializableCodecs.HEX_INT, s -> s.color),
                new StringSerializableField<>(
                        "active",
                        StringSerializableCodecs.BOOLEAN.constant(false),
                        s -> s.active
                ),
                new StringSerializableField<>("input", BusSignalRef.STRINGY_CODEC, s -> s.outputSignal)
        );
    }

    @Nullable
    @Override
    protected AxisAlignedBB createSelectionShape() {
        return new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);
    }

    @Override
    public ActionResultType onClick() {
        this.active = !this.active;
        return ActionResultType.SUCCESS;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setOutputSignal(BusSignalRef outputSignal) {
        this.outputSignal = outputSignal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Button button = (Button) o;
        return color == button.color && active == button.active && outputSignal.equals(button.outputSignal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, active, outputSignal);
    }
}
