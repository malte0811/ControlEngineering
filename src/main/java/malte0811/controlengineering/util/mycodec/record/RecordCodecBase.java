package malte0811.controlengineering.util.mycodec.record;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import malte0811.controlengineering.util.mycodec.tree.TreeElement;
import malte0811.controlengineering.util.mycodec.tree.TreeManager;

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
    public final <B> TreeElement<B> toTree(T in, TreeManager<B> manager) {
        var result = manager.makeTree();
        for (var field : fields) {
            result.put(field.name(), field.toNBT(in, manager));
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
