package malte0811.controlengineering.controlpanels.components.config;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.util.serialization.ListBasedCodec;
import malte0811.controlengineering.util.serialization.serial.BasicCodecParser;

import java.util.Objects;

public record ColorAndSignal(int color, BusSignalRef signal) {
    public static final Codec<ColorAndSignal> CODEC = ListBasedCodec.create(
            "color", BasicCodecParser.HEX_INT, ColorAndSignal::color,
            "signal", BusSignalRef.CODEC, ColorAndSignal::signal,
            ColorAndSignal::new
    );
    public static final ColorAndSignal DEFAULT = new ColorAndSignal(0xff00, new BusSignalRef(0, 0));
}
