package malte0811.controlengineering.util.mycodec;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.serial.PacketBufferStorage;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import malte0811.controlengineering.util.mycodec.tree.TreeElement;
import malte0811.controlengineering.util.mycodec.tree.TreeManager;
import malte0811.controlengineering.util.mycodec.tree.nbt.NBTManager;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

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

    @SuppressWarnings("unchecked")
    default void toSerialUnchecked(SerialStorage out, Object in) {
        toSerial(out, (T) in);
    }

    FastDataResult<T> fromSerial(SerialStorage in);

    default <Owner> CodecField<Owner, T> fieldOf(String name, Function<Owner, T> get) {
        return new CodecField<>(name, get, this);
    }

    default <T2> MyCodec<T2> xmap(Function<T, T2> to, Function<T2, T> from) {
        return new SimpleCodec<>(
                t -> {
                    final var original = fromTree(t);
                    return original != null ? to.apply(original) : null;
                },
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
            Function<? super E, ? extends T> type, Function<? super T, ? extends MyCodec<? extends E>> codec
    ) {
        return dispatch(type, codec, "type", "data");
    }

    default <E>
    MyCodec<E> dispatch(
            Function<? super E, ? extends T> type,
            Function<? super T, ? extends MyCodec<? extends E>> codec,
            String typeKey,
            String valueKey
    ) {
        return new DispatchCodec<>(this, type, codec, typeKey, valueKey);
    }

    default MyCodec<T> copy() {
        return xmap(Function.identity(), Function.identity());
    }

    default T from(FriendlyByteBuf in) {
        return fromSerial(new PacketBufferStorage(in)).get();
    }

    default MyCodec<T> orElse(MyCodec<T> fallback) {
        return new MyCodec<>() {
            @Override
            public <B> TreeElement<B> toTree(T in, TreeManager<B> manager) {
                return MyCodec.this.toTree(in, manager);
            }

            @Nullable
            @Override
            public T fromTree(TreeElement<?> data) {
                final T mainResult = MyCodec.this.fromTree(data);
                if (mainResult != null) {
                    return mainResult;
                } else {
                    return fallback.fromTree(data);
                }
            }

            @Override
            public void toSerial(SerialStorage out, T in) {
                MyCodec.this.toSerial(out, in);
            }

            @Override
            public FastDataResult<T> fromSerial(SerialStorage in) {
                in.pushMark();
                var result = MyCodec.this.fromSerial(in);
                if (result.isError()) {
                    in.resetToMark();
                    result = fallback.fromSerial(in);
                }
                in.popMark();
                return result;
            }
        };
    }
}
