package malte0811.controlengineering.util;

import org.apache.commons.lang3.mutable.Mutable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record LambdaMutable<T>(Supplier<T> getter, Consumer<T> setter) implements Mutable<T> {
    public static <T> Mutable<T> getterOnly(Supplier<T> getter) {
        return new LambdaMutable<>(getter, $ -> {
            throw new RuntimeException("Setting is not supported");
        });
    }

    @Override
    public T getValue() {
        return getter.get();
    }

    @Override
    public void setValue(T value) {
        setter.accept(value);
    }
}
