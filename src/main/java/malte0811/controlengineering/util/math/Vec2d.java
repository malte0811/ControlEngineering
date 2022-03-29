package malte0811.controlengineering.util.math;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public record Vec2d(double x, double y) {
    public static final MyCodec<Vec2d> CODEC = new RecordCodec2<>(
            new CodecField<>("x", Vec2d::x, MyCodecs.DOUBLE),
            new CodecField<>("y", Vec2d::y, MyCodecs.DOUBLE),
            Vec2d::new
    );
    public static final Vec2d ZERO = new Vec2d(0, 0);

    public Vec2d(FriendlyByteBuf in) {
        this(in.readDouble(), in.readDouble());
    }

    public void write(FriendlyByteBuf out) {
        out.writeDouble(x);
        out.writeDouble(y);
    }

    public static Vec2d lerp(Vec2d start, Vec2d end, double time) {
        return new Vec2d(Mth.lerp(time, start.x, end.x), Mth.lerp(time, start.y, end.y));
    }

    public Vec2d scale(double scale) {
        return new Vec2d(x * scale, y * scale);
    }

    public Vec2d subtract(Vec2d rhs) {
        return subtract(rhs.x(), rhs.y());
    }

    public Vec2d subtract(double x, double y) {
        return new Vec2d(this.x - x, this.y - y);
    }

    public Vec2d add(Vec2d rhs) {
        return add(rhs.x(), rhs.y());
    }

    public Vec2d add(double x, double y) {
        return new Vec2d(this.x + x, this.y + y);
    }

    public Vec2d add(Vec2i size) {
        return new Vec2d(x + size.x(), y + size.y());
    }

    public double get(int coord) {
        return coord == 1 ? y : x;
    }

    public Vec2i floor() {
        return new Vec2i(Mth.floor(x), Mth.floor(y));
    }
}
