package malte0811.controlengineering.util.mycodec;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import malte0811.controlengineering.util.mycodec.tree.TreeElement;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class SimpleCodec<Type> implements MyCodec<Type> {
    private final Function<TreeElement<?>, Type> fromTree;
    private final BiConsumer<SerialStorage, Type> toSerial;
    private final Function<SerialStorage, FastDataResult<Type>> fromSerial;

    protected SimpleCodec(
            Function<TreeElement<?>, Type> fromNBT,
            BiConsumer<SerialStorage, Type> toSerial,
            Function<SerialStorage, FastDataResult<Type>> fromSerial
    ) {
        this.fromTree = fromNBT;
        this.toSerial = toSerial;
        this.fromSerial = fromSerial;
    }

    @Nullable
    @Override
    public Type fromTree(TreeElement<?> data) {
        return fromTree.apply(data);
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
