package malte0811.controlengineering.util.mycodec.record;

import com.mojang.datafixers.util.Function4;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import malte0811.controlengineering.util.mycodec.tree.TreeElement;
import malte0811.controlengineering.util.mycodec.tree.TreeStorage;

import javax.annotation.Nullable;

public class RecordCodec4<T, E1, E2, E3, E4> extends RecordCodecBase<T> {
    private final CodecField<T, E1> first;
    private final CodecField<T, E2> second;
    private final CodecField<T, E3> third;
    private final CodecField<T, E4> fourth;
    private final Function4<E1, E2, E3, E4, T> make;

    public RecordCodec4(
            CodecField<T, E1> first,
            CodecField<T, E2> second,
            CodecField<T, E3> third,
            CodecField<T, E4> fourth,
            Function4<E1, E2, E3, E4, T> make
    ) {
        super(first, second, third, fourth);
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.make = make;
    }

    @Nullable
    @Override
    public T fromTree(TreeElement<?> data) {
        if (!(data instanceof TreeStorage tree)) {
            return null;
        }
        var firstVal = first.fromNBT(tree);
        var secondVal = second.fromNBT(tree);
        var thirdVal = third.fromNBT(tree);
        var fourthVal = fourth.fromNBT(tree);
        if (firstVal == null || secondVal == null || thirdVal == null || fourthVal == null) {
            return null;
        }
        return make.apply(firstVal, secondVal, thirdVal, fourthVal);
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
        var maybeFourth = fourth.fromSerial(in);
        if (maybeFourth.isError()) {
            return maybeFourth.propagateError();
        }
        return FastDataResult.success(make.apply(
                maybeFirst.get(), maybeSecond.get(), maybeThird.get(), maybeFourth.get()
        ));
    }
}
