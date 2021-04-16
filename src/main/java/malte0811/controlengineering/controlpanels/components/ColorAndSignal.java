package malte0811.controlengineering.controlpanels.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.bus.BusSignalRef;

import java.util.Objects;

public class ColorAndSignal {
    public static final Codec<ColorAndSignal> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(o -> o.color),
                    BusSignalRef.CODEC.fieldOf("signal").forGetter(o -> o.signal)
            ).apply(inst, ColorAndSignal::new)
    );
    public static final ColorAndSignal DEFAULT = new ColorAndSignal(-1, new BusSignalRef(0, 0));

    private final int color;
    private final BusSignalRef signal;

    public ColorAndSignal(int color, BusSignalRef signal) {
        this.color = color;
        this.signal = signal;
    }

    public int getColor() {
        return color;
    }

    public BusSignalRef getSignal() {
        return signal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorAndSignal config = (ColorAndSignal) o;
        return color == config.color && signal.equals(config.signal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, signal);
    }
}
