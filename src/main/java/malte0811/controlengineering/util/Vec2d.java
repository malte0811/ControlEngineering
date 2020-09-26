package malte0811.controlengineering.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
}
