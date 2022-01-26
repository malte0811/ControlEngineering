package malte0811.controlengineering.util.serialization.mycodec.record;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.serialization.serial.SerialStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.function.Function;

public record CodecField<Owner, Type>(String name, Function<Owner, Type> get, MyCodec<Type> codec) {
    public Tag toNBT(Owner o) {
        return codec.toNBT(get.apply(o));
    }

    public Type fromNBT(CompoundTag tag) {
        return codec.fromNBT(tag.get(name));
    }

    public void toSerial(SerialStorage out, Owner in) {
        codec.toSerial(out, get.apply(in));
    }

    public FastDataResult<Type> fromSerial(SerialStorage in) {
        return codec.fromSerial(in);
    }
}
