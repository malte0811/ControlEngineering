package malte0811.controlengineering.util.serialization.mycodec.record;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.serialization.serial.SerialStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class RecordCodec2<T, E1, E2> extends RecordCodecBase<T> {
    private final CodecField<T, E1> first;
    private final CodecField<T, E2> second;
    private final BiFunction<E1, E2, T> make;

    public RecordCodec2(CodecField<T, E1> first, CodecField<T, E2> second, BiFunction<E1, E2, T> make) {
        super(first, second);
        this.first = first;
        this.second = second;
        this.make = make;
    }

    @Nullable
    @Override
    public T fromNBT(Tag data) {
        if (!(data instanceof CompoundTag compound)) {
            return null;
        }
        var firstVal = first.fromNBT(compound);
        var secondVal = second.fromNBT(compound);
        if (firstVal == null || secondVal == null) {
            return null;
        }
        return make.apply(firstVal, secondVal);
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
