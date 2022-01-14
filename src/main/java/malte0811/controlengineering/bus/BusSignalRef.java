package malte0811.controlengineering.bus;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.serialization.ListBasedCodec;

public record BusSignalRef(int line, int color) {
    public static final Codec<BusSignalRef> CODEC = ListBasedCodec.create(
            "line", Codec.INT, BusSignalRef::line,
            "color", Codec.INT, BusSignalRef::color,
            BusSignalRef::new
    );
    public static final BusSignalRef DEFAULT = new BusSignalRef(0, 0);

    public BusState singleSignalState(int value) {
        return BusState.EMPTY.with(this, value);
    }
}
