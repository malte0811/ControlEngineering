package malte0811.controlengineering.util.mycodec;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import malte0811.controlengineering.util.mycodec.tree.TreeElement;
import malte0811.controlengineering.util.mycodec.tree.TreeManager;
import malte0811.controlengineering.util.mycodec.tree.TreeStorage;

import javax.annotation.Nullable;
import java.util.function.Function;

public record DispatchCodec<Type, Instance>(
        MyCodec<Type> typeCodec,
        Function<? super Instance, ? extends Type> type,
        Function<? super Type, ? extends MyCodec<? extends Instance>> codec
) implements MyCodec<Instance> {
    @Override
    public <B> TreeElement<B> toTree(Instance in, TreeManager<B> manager) {
        var result = manager.makeTree();
        var type = type().apply(in);
        result.put("type", typeCodec.toTree(type, manager));
        var instanceCodec = codec().apply(type);
        result.put("data", toNBT(instanceCodec, in, manager));
        return result;
    }

    @SuppressWarnings("unchecked")
    private <I extends Instance, B> TreeElement<B> toNBT(MyCodec<I> codec, Instance inst, TreeManager<B> manager) {
        return codec.toTree((I) inst, manager);
    }

    @Nullable
    @Override
    public Instance fromTree(TreeElement<?> data) {
        if (!(data instanceof TreeStorage tree)) {
            return null;
        }
        var type = typeCodec.fromTree(tree.get("type"));
        var instanceCodec = codec.apply(type);
        return instanceCodec.fromTree(tree.get("data"));
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
