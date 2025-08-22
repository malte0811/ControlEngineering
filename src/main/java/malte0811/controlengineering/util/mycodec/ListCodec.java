package malte0811.controlengineering.util.mycodec;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;

import java.util.ArrayList;
import java.util.List;

public class ListCodec<T> implements MyCodec<List<T>> {
    private final MyCodec<T> inner;
    private final Codec<List<T>> dfuCodec;

    public ListCodec(MyCodec<T> inner) {
        this.inner = inner;
        this.dfuCodec = inner.toDFUCodec().listOf();
    }

    @Override
    public Codec<List<T>> toDFUCodec() {
        return dfuCodec;
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
