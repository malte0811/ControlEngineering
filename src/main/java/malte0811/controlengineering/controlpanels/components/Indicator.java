package malte0811.controlengineering.controlpanels.components;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class Indicator extends PanelComponent<Indicator> {
    private final int color;
    @Nonnull
    private final BusSignalRef signal;
    private int rsValue;

    public Indicator() {
        this(-1, new BusSignalRef(0, 0), 0);
    }

    public Indicator(int color, @Nonnull BusSignalRef signal, int rsValue) {
        super(new Vec2d(1, 1));
        this.color = color;
        this.signal = signal;
        this.rsValue = rsValue;
    }

    @Override
    public BusState getEmittedState() {
        return BusState.EMPTY;
    }

    @Override
    public void updateTotalState(BusState state) {
        rsValue = state.getSignal(signal);
    }

    @Nullable
    @Override
    protected AxisAlignedBB createSelectionShape() {
        return null;
    }

    @Override
    public ActionResultType onClick() {
        return ActionResultType.PASS;
    }

    public static StringSerializer<Indicator> createCodec() {
        return new StringSerializer<>(
                ReflectionUtils.findConstructor(Indicator.class, int.class, BusSignalRef.class, int.class),
                new StringSerializableField<>("color", StringSerializableCodecs.HEX_INT, s -> s.color),
                new StringSerializableField<>("input", BusSignalRef.STRINGY_CODEC, s -> s.signal),
                new StringSerializableField<>("value", StringSerializableCodecs.INT.constant(0), s -> s.rsValue)
        );
    }

    public int getColor() {
        return color;
    }

    public int getRsValue() {
        return rsValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Indicator indicator = (Indicator) o;
        return color == indicator.color && rsValue == indicator.rsValue && signal.equals(indicator.signal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, signal, rsValue);
    }
}
