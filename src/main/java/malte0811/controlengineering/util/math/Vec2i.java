package malte0811.controlengineering.util.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import java.util.Comparator;
import java.util.Objects;
import java.util.StringJoiner;

public record Vec2i(int x, int y) implements Comparable<Vec2i> {
    public static final Codec<Vec2i> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.INT.fieldOf("x").forGetter(v -> v.x),
                    Codec.INT.fieldOf("y").forGetter(v -> v.y)
            ).apply(inst, Vec2i::new)
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
}
