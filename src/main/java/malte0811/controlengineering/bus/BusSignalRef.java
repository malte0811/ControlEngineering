package malte0811.controlengineering.bus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.util.serialization.StringSerializableCodec;

import java.util.Objects;

public class BusSignalRef {
    public static final Codec<BusSignalRef> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.INT.fieldOf("line").forGetter(ref -> ref.line),
                    Codec.INT.fieldOf("color").forGetter(ref -> ref.color)
            ).apply(inst, BusSignalRef::new)
    );

    public static final StringSerializableCodec<BusSignalRef> STRINGY_CODEC = StringSerializableCodec.fromCodec(
            BusSignalRef.CODEC,
            (line, color) -> new BusSignalRef(Integer.parseInt(line), Integer.parseInt(color))
    );

    public final int line;
    public final int color;

    public BusSignalRef(int line, int color) {
        this.line = line;
        this.color = color;
    }

    public BusState singleSignalState(int value) {
        return new BusState(line + 1).with(this, value);
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
