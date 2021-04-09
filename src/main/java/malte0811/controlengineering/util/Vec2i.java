package malte0811.controlengineering.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Objects;
import java.util.StringJoiner;

public class Vec2i implements Comparable<Vec2i> {
    public static final Codec<Vec2i> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.INT.fieldOf("x").forGetter(v -> v.x),
                    Codec.INT.fieldOf("y").forGetter(v -> v.y)
            ).apply(inst, Vec2i::new)
    );
    public final int x;
    public final int y;

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vec2i(PacketBuffer buffer) {
        this(buffer.readVarInt(), buffer.readVarInt());
    }

    public void write(PacketBuffer out) {
        out.writeVarInt(x);
        out.writeVarInt(y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec2i vec2i = (Vec2i) o;
        return x == vec2i.x && y == vec2i.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Vec2i.class.getSimpleName() + "[", "]")
                .add("x=" + x)
                .add("y=" + y)
                .toString();
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
}
