package malte0811.controlengineering.util.serialization.mycodec;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.serialization.serial.SerialStorage;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface MyCodec<T> {
    Tag toNBT(T in);

    @Nullable
    T fromNBT(Tag data);

    // TODO split into network and string directly?
    void toSerial(SerialStorage out, T in);

    FastDataResult<T> fromSerial(SerialStorage in);

    default <T2> MyCodec<T2> xmap(Function<T, T2> to, Function<T2, T> from) {
        return new SimpleCodec<>(
                Tag.class,
                t2 -> toNBT(from.apply(t2)),
                t -> to.apply(fromNBT(t)),
                (s, t2) -> toSerial(s, from.apply(t2)),
                s -> fromSerial(s).map(to)
        );
    }

    default <T2> MyCodec<T2> flatXmap(Function<T, FastDataResult<T2>> to, Function<T2, T> from) {
        return new SimpleCodec<>(
                Tag.class,
                t2 -> toNBT(from.apply(t2)),
                t -> to.apply(fromNBT(t)).orElse(null),
                (s, t2) -> toSerial(s, from.apply(t2)),
                s -> fromSerial(s).flatMap(to)
        );
    }

    default T fromNBT(Tag data, Supplier<T> fallback) {
        return Objects.requireNonNullElseGet(fromNBT(data), fallback);
    }

    default <E>
    MyCodec<E> dispatch(
            Function<? super E, ? extends T> type,
            Function<? super T, ? extends MyCodec<? extends E>> codec
    ) {
        return new DispatchCodec<>(this, type, codec);
    }
}
