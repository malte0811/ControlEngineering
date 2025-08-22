package malte0811.controlengineering.util.mycodec.record;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import malte0811.dualcodecs.EntryListCodec;
import malte0811.dualcodecs.EntryListCodecs;

import java.util.function.BiFunction;

public class RecordCodec2<T, E1, E2> extends RecordCodecBase<T> {
    private final CodecField<T, E1> first;
    private final CodecField<T, E2> second;
    private final BiFunction<E1, E2, T> make;

    public RecordCodec2(CodecField<T, E1> first, CodecField<T, E2> second, BiFunction<E1, E2, T> make) {
        super(
                EntryListCodecs.composite(first.mapCodec(), first.get(), second.mapCodec(), second.get(), make),
                first, second
        );
        this.first = first;
        this.second = second;
        this.make = make;
    }

    @Override
    public FastDataResult<T> fromSerial(SerialStorage in) {
        var maybeFirst = first.fromSerial(in);
        if (maybeFirst.isError()) {
            return maybeFirst.propagateError();
        }
        var maybeSecond = second.fromSerial(in);
        if (maybeSecond.isError()) {
            return maybeSecond.propagateError();
        }
        return FastDataResult.success(make.apply(maybeFirst.get(), maybeSecond.get()));
    }
}
