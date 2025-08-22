package malte0811.controlengineering.util.mycodec;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;

import java.util.Objects;
import java.util.function.Function;

public final class DispatchCodec<Type, Instance> implements MyCodec<Instance> {
    private final MyCodec<Type> typeCodec;
    private final Function<? super Instance, ? extends Type> type;
    private final Function<? super Type, ? extends MyCodec<? extends Instance>> codec;
    private final Codec<Instance> dfuCodec;

    public DispatchCodec(
            MyCodec<Type> typeCodec,
            Function<? super Instance, ? extends Type> type,
            Function<? super Type, ? extends MyCodec<? extends Instance>> codec,
            String typeKey,
            String dataKey
    ) {
        this.typeCodec = typeCodec;
        this.type = type;
        this.codec = codec;
        this.dfuCodec = typeCodec.dispatch(type, t -> codec.apply(t).toDFUCodec(), typeKey, dataKey);
    }

    @Override
    public Codec<Instance> toDFUCodec() {
        return this.dfuCodec;
    }

    @Override
    public void toSerial(SerialStorage out, Instance in) {
        var type = this.type.apply(in);
        typeCodec.toSerial(out, type);
        var instanceCodec = codec.apply(type);
        toSerial(out, instanceCodec, in);
    }

    @SuppressWarnings("unchecked")
    private <I extends Instance> void toSerial(SerialStorage out, MyCodec<I> codec, Instance inst) {
        codec.toSerial(out, (I) inst);
    }

    @Override
    public FastDataResult<Instance> fromSerial(SerialStorage in) {
        var maybeType = typeCodec.fromSerial(in);
        if (maybeType.isError()) {
            return maybeType.propagateError();
        }
        var instanceCodec = codec.apply(maybeType.get());
        return instanceCodec.fromSerial(in).map(i -> i);
    }
}
