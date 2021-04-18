package malte0811.controlengineering.bus;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.serialization.ListBasedCodec;

import java.util.Objects;

public class BusSignalRef {
    public static final Codec<BusSignalRef> CODEC = ListBasedCodec.create(
            "line", Codec.INT, ref -> ref.line,
            "color", Codec.INT, ref -> ref.color,
            BusSignalRef::new
    );

    public final int line;
    public final int color;

    public BusSignalRef(int line, int color) {
        this.line = line;
        this.color = color;
    }

    public BusState singleSignalState(int value) {
        return BusState.EMPTY.with(this, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusSignalRef that = (BusSignalRef) o;
        return line == that.line &&
                color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, color);
    }
}
