package malte0811.controlengineering.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public class FastDataResult<T> {
    @Nullable
    private final T value;
    @Nullable
    private final String error;

    private FastDataResult(@Nullable T value, @Nullable String error) {
        Preconditions.checkState(value == null || error == null);
        this.value = value;
        this.error = error;
    }

    public static <T> FastDataResult<T> success(T value) {
        return new FastDataResult<>(value, null);
    }

    public static <T> FastDataResult<T> error(String message) {
        Preconditions.checkState(message != null);
        return new FastDataResult<>(null, message);
    }

    public boolean isError() {
        return error != null;
    }

    public String getErrorMessage() {
        return Objects.requireNonNull(error);
    }

    public <T2> FastDataResult<T2> propagateError() {
        Preconditions.checkState(isError());
        return FastDataResult.error(getErrorMessage());
    }

    public T get() {
        Preconditions.checkState(!isError());
        return value;
    }

    public <T2> FastDataResult<T2> flatMap(Function<T, FastDataResult<T2>> to) {
        if (isError()) {
            return propagateError();
        } else {
            return to.apply(get());
        }
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
