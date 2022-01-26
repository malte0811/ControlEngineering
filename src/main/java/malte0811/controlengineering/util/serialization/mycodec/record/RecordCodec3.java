package malte0811.controlengineering.util.serialization.mycodec.record;

import com.mojang.datafixers.util.Function3;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.serialization.serial.SerialStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public class RecordCodec3<T, E1, E2, E3> extends RecordCodecBase<T> {
    private final CodecField<T, E1> first;
    private final CodecField<T, E2> second;
    private final CodecField<T, E3> third;
    private final Function3<E1, E2, E3, T> make;

    public RecordCodec3(
            CodecField<T, E1> first, CodecField<T, E2> second, CodecField<T, E3> third, Function3<E1, E2, E3, T> make
    ) {
        super(first, second, third);
        this.first = first;
        this.second = second;
        this.third = third;
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
        var thirdVal = third.fromNBT(compound);
        if (firstVal == null || secondVal == null || thirdVal == null) {
            return null;
        }
        return make.apply(firstVal, secondVal, thirdVal);
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
        var maybeThird = third.fromSerial(in);
        if (maybeThird.isError()) {
            return maybeThird.propagateError();
        }
        return FastDataResult.success(make.apply(maybeFirst.get(), maybeSecond.get(), maybeThird.get()));
    }
}
