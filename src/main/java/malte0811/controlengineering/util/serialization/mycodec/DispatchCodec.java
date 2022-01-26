package malte0811.controlengineering.util.serialization.mycodec;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.serialization.serial.SerialStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.function.Function;

public record DispatchCodec<Type, Instance>(
        MyCodec<Type> typeCodec,
        Function<? super Instance, ? extends Type> type,
        Function<? super Type, ? extends MyCodec<? extends Instance>> codec
) implements MyCodec<Instance> {
    @Override
    public Tag toNBT(Instance in) {
        var result = new CompoundTag();
        var type = type().apply(in);
        result.put("type", typeCodec.toNBT(type));
        var instanceCodec = codec().apply(type);
        result.put("data", toNBT(instanceCodec, in));
        return result;
    }

    @SuppressWarnings("unchecked")
    private <I extends Instance> Tag toNBT(MyCodec<I> codec, Instance inst) {
        return codec.toNBT((I) inst);
    }

    @Nullable
    @Override
    public Instance fromNBT(Tag data) {
        if (!(data instanceof CompoundTag compound)) {
            return null;
        }
        var type = typeCodec.fromNBT(compound.get("type"));
        var instanceCodec = codec.apply(type);
        return instanceCodec.fromNBT(compound.get("data"));
    }

    @Override
    public void toSerial(SerialStorage out, Instance in) {
        var type = type().apply(in);
        typeCodec.toSerial(out, type);
        var instanceCodec = codec().apply(type);
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
