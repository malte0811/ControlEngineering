package malte0811.controlengineering.controlpanels.components.config;

import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;
import malte0811.controlengineering.util.serialization.mycodec.record.CodecField;
import malte0811.controlengineering.util.serialization.mycodec.record.RecordCodec2;

public record ColorAndSignal(int color, BusSignalRef signal) {
    public static final MyCodec<ColorAndSignal> CODEC = new RecordCodec2<>(
            new CodecField<>("color", ColorAndSignal::color, MyCodecs.HEX_INTEGER),
            new CodecField<>("signal", ColorAndSignal::signal, BusSignalRef.CODEC),
            ColorAndSignal::new
    );
    public static final ColorAndSignal DEFAULT = new ColorAndSignal(0xff00, BusSignalRef.DEFAULT);
}
