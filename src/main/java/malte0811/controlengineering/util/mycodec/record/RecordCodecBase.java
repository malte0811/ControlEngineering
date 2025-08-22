package malte0811.controlengineering.util.mycodec.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;

import java.util.Arrays;
import java.util.List;

public abstract class RecordCodecBase<T> implements MyCodec<T> {
    private final List<CodecField<T, ?>> fields;
    private final MapCodec<T> dfuCodec;

    @SafeVarargs
    protected RecordCodecBase(MapCodec<T> dfuCodec, CodecField<T, ?>... fields) {
        this.fields = Arrays.asList(fields);
        this.dfuCodec = dfuCodec;
    }

    public List<CodecField<T, ?>> getFields() {
        return fields;
    }

    @Override
    public Codec<T> toDFUCodec() {
        return dfuCodec.codec();
    }

    @Override
    public final void toSerial(SerialStorage out, T in) {
        for (var field : fields) {
            field.toSerial(out, in);
        }
    }
}
