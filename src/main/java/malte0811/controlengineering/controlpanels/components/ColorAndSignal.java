package malte0811.controlengineering.controlpanels.components;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.util.serialization.ListBasedCodec;
import malte0811.controlengineering.util.serialization.serial.BasicCodecParser;

import java.util.Objects;

public class ColorAndSignal {
    public static final Codec<ColorAndSignal> CODEC = ListBasedCodec.create(
            "color", BasicCodecParser.HEX_INT, ColorAndSignal::getColor,
            "signal", BusSignalRef.CODEC, ColorAndSignal::getSignal,
            ColorAndSignal::new
    );
    public static final ColorAndSignal DEFAULT = new ColorAndSignal(0xff00, new BusSignalRef(0, 0));

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
