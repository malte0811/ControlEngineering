package malte0811.controlengineering.util.mycodec;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import malte0811.controlengineering.util.mycodec.tree.TreeElement;
import malte0811.controlengineering.util.mycodec.tree.TreeManager;
import malte0811.controlengineering.util.mycodec.tree.TreeStorageList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record ListCodec<T>(MyCodec<T> inner) implements MyCodec<List<T>> {
    @Override
    public <B> TreeElement<B> toTree(List<T> in, TreeManager<B> manager) {
        var result = manager.makeList();
        for (T element : in) {
            result.add(inner.toTree(element, manager));
        }
        return result;
    }

    @Nullable
    @Override
    public List<T> fromTree(TreeElement<?> data) {
        if (!(data instanceof TreeStorageList<?> list))
            return null;
        List<T> result = new ArrayList<>();
        for (var node : list) {
            var element = inner.fromTree(node);
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
