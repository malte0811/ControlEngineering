package malte0811.controlengineering.util.mycodec;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import malte0811.controlengineering.util.mycodec.tree.TreeElement;
import malte0811.controlengineering.util.mycodec.tree.TreeManager;
import malte0811.controlengineering.util.mycodec.tree.nbt.NBTManager;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface MyCodec<T> {
    <B> TreeElement<B> toTree(T in, TreeManager<B> manager);

    @Nullable
    T fromTree(TreeElement<?> data);

    // TODO split into network and string directly?
    void toSerial(SerialStorage out, T in);

    FastDataResult<T> fromSerial(SerialStorage in);

    default <T2> MyCodec<T2> xmap(Function<T, T2> to, Function<T2, T> from) {
        return new SimpleCodec<>(
                TreeElement.class,
                t -> to.apply(fromTree(t)),
                (s, t2) -> toSerial(s, from.apply(t2)),
                s -> fromSerial(s).map(to)
        ) {
            @Override
            public <B> TreeElement<B> toTree(T2 in, TreeManager<B> manager) {
                return MyCodec.this.toTree(from.apply(in), manager);
            }
        };
    }

    default <T2> MyCodec<T2> flatXmap(Function<T, FastDataResult<T2>> to, Function<T2, T> from) {
        return new SimpleCodec<>(
                TreeElement.class,
                t -> to.apply(fromTree(t)).orElse(null),
                (s, t2) -> toSerial(s, from.apply(t2)),
                s -> fromSerial(s).flatMap(to)
        ) {

            @Override
            public <B> TreeElement<B> toTree(T2 in, TreeManager<B> manager) {
                return MyCodec.this.toTree(from.apply(in), manager);
            }
        };
    }

    default T fromNBT(Tag data) {
        return fromTree(NBTManager.INSTANCE.of(data));
    }

    default T fromNBT(Tag data, Supplier<T> fallback) {
        return Objects.requireNonNullElseGet(fromNBT(data), fallback);
    }

    default Tag toNBT(T data) {
        return toTree(data, NBTManager.INSTANCE).getDirect();
    }

    default <E>
    MyCodec<E> dispatch(
            Function<? super E, ? extends T> type,
            Function<? super T, ? extends MyCodec<? extends E>> codec
    ) {
        return new DispatchCodec<>(this, type, codec);
    }
}
