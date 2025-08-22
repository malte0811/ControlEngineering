package malte0811.controlengineering.util.mycodec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.serial.PacketBufferStorage;
import malte0811.controlengineering.util.mycodec.serial.SerialStorage;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface MyCodec<T> {
    Codec<T> toDFUCodec();

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
                toDFUCodec().xmap(to, from),
                (s, t2) -> toSerial(s, from.apply(t2)),
                s -> fromSerial(s).map(to)
        );
    }

    default <T2> MyCodec<T2> flatXmap(Function<T, FastDataResult<T2>> to, Function<T2, T> from) {
        return new SimpleCodec<>(
                toDFUCodec().flatXmap(
                        t -> to.apply(t).toDFU(), t2 -> DataResult.success(from.apply(t2))
                ),
                (s, t2) -> toSerial(s, from.apply(t2)),
                s -> fromSerial(s).flatMap(to)
        );
    }

    @Nullable
    default T fromNBT(Tag data) {
        var result = toDFUCodec().parse(NbtOps.INSTANCE, data);
        return result.mapOrElse(Function.identity(), (err) -> null);
    }

    default T fromNBT(Tag data, Supplier<T> fallback) {
        return Objects.requireNonNullElseGet(fromNBT(data), fallback);
    }

    default Tag toNBT(T data) {
        return toDFUCodec().encodeStart(NbtOps.INSTANCE, data).getOrThrow();
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

    default StreamCodec<FriendlyByteBuf, T> streamCodec() {
        return new StreamCodec<FriendlyByteBuf, T>() {
            @Override
            public T decode(FriendlyByteBuf in) {
                return fromSerial(new PacketBufferStorage(in)).get();
            }

            @Override
            public void encode(FriendlyByteBuf o, T t) {
                toSerial(new PacketBufferStorage(o), t);
            }
        };
    }

    default MyCodec<T> orElse(MyCodec<T> fallback) {
        final var dfuCodec = Codec.either(toDFUCodec(), fallback.toDFUCodec())
                .xmap(e -> e.map(Function.identity(), Function.identity()), Either::left);
        return new MyCodec<>() {
            @Override
            public Codec<T> toDFUCodec() {
                return dfuCodec;
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
