package malte0811.controlengineering.util.mycodec.record;

import com.mojang.serialization.MapCodec;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;

import java.util.function.Function;

public record CodecField<Owner, Type>(String name, Function<Owner, Type> get, MyCodec<Type> codec) {
    public MapCodec<Type> mapCodec() {
        return codec.toDFUCodec().fieldOf(name);
    }

    public void toSerial(SerialStorage out, Owner in) {
        codec.toSerial(out, get.apply(in));
    }

    public FastDataResult<Type> fromSerial(SerialStorage in) {
        return codec.fromSerial(in);
    }
}
