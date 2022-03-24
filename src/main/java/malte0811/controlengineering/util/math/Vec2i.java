package malte0811.controlengineering.util.math;

import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;
import malte0811.controlengineering.util.serialization.mycodec.record.CodecField;
import malte0811.controlengineering.util.serialization.mycodec.record.RecordCodec2;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.Comparator;

public record Vec2i(int x, int y) implements Comparable<Vec2i> {
    public static final MyCodec<Vec2i> CODEC = new RecordCodec2<>(
            new CodecField<>("x", Vec2i::x, MyCodecs.INTEGER),
            new CodecField<>("y", Vec2i::y, MyCodecs.INTEGER),
            Vec2i::new
    );

    public Vec2i(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readVarInt());
    }

    public void write(FriendlyByteBuf out) {
        out.writeVarInt(x);
        out.writeVarInt(y);
    }

    public Vec2i add(Vec2i other) {
        return new Vec2i(x + other.x, y + other.y);
    }

    private static final Comparator<Vec2i> COMPARATOR = Comparator.<Vec2i>comparingInt(v -> v.x)
            .thenComparingInt(v -> v.y);

    @Override
    public int compareTo(@Nonnull Vec2i o) {
        return COMPARATOR.compare(this, o);
    }

    public Vec2d subtract(Vec2d rhs) {
        return new Vec2d(x() - rhs.x(), y() - rhs.y());
    }
}
