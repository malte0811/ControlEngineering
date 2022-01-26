package malte0811.controlengineering.util.serialization.mycodec;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.serialization.serial.SerialStorage;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record ListCodec<T>(MyCodec<T> inner) implements MyCodec<List<T>> {
    @Override
    public Tag toNBT(List<T> in) {
        ListTag result = new ListTag();
        for (T element : in) {
            result.add(inner.toNBT(element));
        }
        return result;
    }

    @Nullable
    @Override
    public List<T> fromNBT(Tag data) {
        if (!(data instanceof ListTag list))
            return null;
        List<T> result = new ArrayList<>();
        for (Tag t : list) {
            var element = inner.fromNBT(t);
            if (element != null) {
                result.add(element);
            }
        }
        return result;
    }

    @Override
    public void toSerial(SerialStorage out, List<T> in) {
        out.writeInt(in.size());
        for (T element : in) {
            inner.toSerial(out, element);
        }
    }

    @Override
    public FastDataResult<List<T>> fromSerial(SerialStorage in) {
        var maybeLength = in.readInt();
        if (maybeLength.isError()) {
            return maybeLength.propagateError();
        }
        final int length = maybeLength.get();
        List<T> result = new ArrayList<>(length);
        for (int i = 0; i < length; ++i) {
            var maybeNext = inner.fromSerial(in);
            if (!maybeNext.isError()) {
                result.add(maybeNext.get());
            }
            //TODO handle partial errors?
        }
        return FastDataResult.success(result);
    }
}
