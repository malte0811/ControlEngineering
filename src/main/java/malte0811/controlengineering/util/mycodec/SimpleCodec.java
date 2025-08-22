package malte0811.controlengineering.util.mycodec;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record SimpleCodec<Type>(
        Codec<Type> dfuCodec, BiConsumer<SerialStorage, Type> toSerial, Function<SerialStorage, FastDataResult<Type>> fromSerial
) implements MyCodec<Type> {
    @Override
    public Codec<Type> toDFUCodec() {
        return this.dfuCodec;
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
