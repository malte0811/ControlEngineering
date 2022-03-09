package malte0811.controlengineering.util.serialization.mycodec.record;

import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.serialization.serial.SerialStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Arrays;
import java.util.List;

public abstract class RecordCodecBase<T> implements MyCodec<T> {
    private final List<CodecField<T, ?>> fields;

    @SafeVarargs
    protected RecordCodecBase(CodecField<T, ?>... fields) {
        this.fields = Arrays.asList(fields);
    }

    public List<CodecField<T, ?>> getFields() {
        return fields;
    }

    @Override
    public final Tag toNBT(T in) {
        var result = new CompoundTag();
        for (var field : fields) {
            result.put(field.name(), field.toNBT(in));
        }
        return result;
    }

    @Override
    public final void toSerial(SerialStorage out, T in) {
        for (var field : fields) {
            field.toSerial(out, in);
        }
    }
}
