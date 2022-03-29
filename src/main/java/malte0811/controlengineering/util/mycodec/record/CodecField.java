package malte0811.controlengineering.util.mycodec.record;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import malte0811.controlengineering.util.mycodec.tree.TreeElement;
import malte0811.controlengineering.util.mycodec.tree.TreeManager;
import malte0811.controlengineering.util.mycodec.tree.TreeStorage;

import java.util.function.Function;

public record CodecField<Owner, Type>(String name, Function<Owner, Type> get, MyCodec<Type> codec) {
    public <B> TreeElement<B> toNBT(Owner o, TreeManager<B> manager) {
        return codec.toTree(get.apply(o), manager);
    }

    public Type fromNBT(TreeStorage<?> tag) {
        return codec.fromTree(tag.get(name));
    }

    public void toSerial(SerialStorage out, Owner in) {
        codec.toSerial(out, get.apply(in));
    }

    public FastDataResult<Type> fromSerial(SerialStorage in) {
        return codec.fromSerial(in);
    }
}
