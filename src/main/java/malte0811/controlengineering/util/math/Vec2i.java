package malte0811.controlengineering.util.math;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.Comparator;

public record Vec2i(int x, int y) implements Comparable<Vec2i> {
    public static final MyCodec<Vec2i> CODEC = new RecordCodec2<>(
            new CodecField<>("x", Vec2i::x, MyCodecs.INTEGER),
            new CodecField<>("y", Vec2i::y, MyCodecs.INTEGER),
            Vec2i::new
    );
    public static final Vec2i ZERO = new Vec2i(0, 0);

    public Vec2i(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readVarInt());
    }

    public void write(FriendlyByteBuf out) {
        out.writeVarInt(x);
        out.writeVarInt(y);
    }

    public Vec2i add(Vec2i other) {
        return add(other.x, other.y);
    }

    public Vec2i add(int x, int y) {
        return new Vec2i(this.x + x, this.y + y);
    }

    private static final Comparator<Vec2i> COMPARATOR = Comparator.<Vec2i>comparingInt(v -> v.x)
            .thenComparingInt(v -> v.y);

    @Override
    public int compareTo(@Nonnull Vec2i o) {
        return COMPARATOR.compare(this, o);
    }

    public Vec2i subtract(Vec2i rhs) {
        return new Vec2i(x() - rhs.x(), y() - rhs.y());
    }
    public Vec2d subtract(Vec2d rhs) {
        return new Vec2d(x() - rhs.x(), y() - rhs.y());
    }

    @Override
    public String toString() {
        return String.format("Vec2i [x=%d, y=%d]", x, y);
    }
}
