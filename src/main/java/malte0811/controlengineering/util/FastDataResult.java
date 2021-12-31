package malte0811.controlengineering.util;

import blusunrize.immersiveengineering.api.utils.FastEither;
import com.google.common.base.Preconditions;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.Contract;

import java.util.Optional;
import java.util.function.Function;

public class FastDataResult<T> {
    private final FastEither<T, String> valueOrError;

    private FastDataResult(FastEither<T, String> valueOrError) {
        this.valueOrError = valueOrError;
    }

    public static <T> FastDataResult<T> success(T value) {
        return new FastDataResult<>(FastEither.left(value));
    }

    public static <T> FastDataResult<T> error(String message) {
        return new FastDataResult<>(FastEither.right(message));
    }

    public boolean isError() {
        return valueOrError.isRight();
    }

    public String getErrorMessage() {
        return valueOrError.rightNonnull();
    }

    public <T2> FastDataResult<T2> propagateError() {
        Preconditions.checkState(isError());
        return FastDataResult.error(getErrorMessage());
    }

    public T get() {
        return valueOrError.leftNonnull();
    }

    public <T2> FastDataResult<T2> flatMapDFU(Function<T, DataResult<T2>> map) {
        if (isError()) {
            return propagateError();
        }
        DataResult<T2> result = map.apply(get());
        Optional<DataResult.PartialResult<T2>> maybeError = result.error();
        if (maybeError.isPresent()) {
            return error(maybeError.get().message());
        }
        return success(result.result().get());
    }

    public <T2> FastDataResult<T2> map(Function<T, T2> map) {
        if (isError()) {
            return propagateError();
        }
        return success(map.apply(get()));
    }

    @Contract("!null -> !null")
    public T orElse(T fallback) {
        if (isError()) {
            return fallback;
        } else {
            return get();
        }
    }
}
