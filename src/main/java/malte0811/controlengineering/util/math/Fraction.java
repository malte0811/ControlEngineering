package malte0811.controlengineering.util.math;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;

public record Fraction(int numerator, int denominator) {
    public static final MyCodec<Fraction> CODEC = new RecordCodec2<>(
            new CodecField<>("numerator", Fraction::numerator, MyCodecs.INTEGER),
            new CodecField<>("denominator", Fraction::denominator, MyCodecs.INTEGER),
            Fraction::new
    );
    public static final Fraction ONE = new Fraction(1, 1);

    public Fraction {
        Preconditions.checkArgument(numerator > 0);
        Preconditions.checkArgument(denominator > 0);
    }

    public int apply(int in) {
        return (in * numerator) / denominator;
    }

    @Override
    public String toString() {
        return numerator + "/" + denominator;
    }
}
