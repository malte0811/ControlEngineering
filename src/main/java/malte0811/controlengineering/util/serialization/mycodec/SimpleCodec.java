package malte0811.controlengineering.util.serialization.mycodec;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.serialization.serial.SerialStorage;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record SimpleCodec<NBT extends Tag, Type>(
        Class<NBT> nbtType,
        Function<Type, NBT> toNBT,
        Function<NBT, Type> fromNBT,
        BiConsumer<SerialStorage, Type> toSerial,
        Function<SerialStorage, FastDataResult<Type>> fromSerial
) implements MyCodec<Type> {
    @Override
    public Tag toNBT(Type in) {
        return toNBT.apply(in);
    }

    @Nullable
    @Override
    public Type fromNBT(Tag data) {
        if (data != null && nbtType.isAssignableFrom(data.getClass())) {
            return fromNBT.apply(nbtType.cast(data));
        }
        return null;
    }

    @Override
    public void toSerial(SerialStorage out, Type in) {
        toSerial.accept(out, in);
    }

    @Override
    public FastDataResult<Type> fromSerial(SerialStorage in) {
        return fromSerial.apply(in);
    }
}
