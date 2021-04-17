package malte0811.controlengineering.util.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public class Vec2d {
    public static final Codec<Vec2d> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.DOUBLE.fieldOf("x").forGetter(v -> v.x),
                    Codec.DOUBLE.fieldOf("y").forGetter(v -> v.y)
            ).apply(inst, Vec2d::new)
    );

    public final double x;
    public final double y;

    public Vec2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2d(PacketBuffer in) {
        this(in.readDouble(), in.readDouble());
    }

    public void write(PacketBuffer out) {
        out.writeDouble(x);
        out.writeDouble(y);
    }

    public static Vec2d lerp(Vec2d start, Vec2d end, double time) {
        return new Vec2d(MathHelper.lerp(time, start.x, end.x), MathHelper.lerp(time, start.y, end.y));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec2d vec2d = (Vec2d) o;
        return Double.compare(vec2d.x, x) == 0 &&
                Double.compare(vec2d.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public Vec2d scale(double scale) {
        return new Vec2d(x * scale, y * scale);
    }

    public Vec2d subtract(Vec2d rhs) {
        return new Vec2d(x - rhs.x, y - rhs.y);
    }

    public Vec2d add(Vec2d size) {
        return new Vec2d(x + size.x, y + size.y);
    }

    public Vec2d add(Vec2i size) {
        return new Vec2d(x + size.x, y + size.y);
    }

    public double get(int coord) {
        return coord == 1 ? y : x;
    }

    public Vec2i floor() {
        return new Vec2i(MathHelper.floor(x), MathHelper.floor(y));
    }
}
