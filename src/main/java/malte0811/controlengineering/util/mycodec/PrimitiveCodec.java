package malte0811.controlengineering.util.mycodec;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import malte0811.controlengineering.util.mycodec.tree.TreePrimitive;
import malte0811.controlengineering.util.mycodec.tree.nbt.NBTPrimitive;
import net.minecraft.nbt.IntTag;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class PrimitiveCodec<T> extends SimpleCodec<T> {
    protected PrimitiveCodec(
            Function<TreePrimitive<?>, T> fromNBT,
            BiConsumer<SerialStorage, T> toSerial,
            Function<SerialStorage, FastDataResult<T>> fromSerial
    ) {
        super(ele -> fromNBT.apply(
                ele instanceof TreePrimitive<?> primitive ? primitive : new NBTPrimitive(IntTag.valueOf(0))
        ), toSerial, fromSerial);
    }
}
