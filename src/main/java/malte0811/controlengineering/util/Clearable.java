package malte0811.controlengineering.util;

import com.mojang.datafixers.util.Pair;
import net.minecraftforge.common.util.NonNullConsumer;

import java.util.Objects;

public class Clearable<T> {
    private T value;

    private Clearable(T value) {
        this.value = value;
    }

    public static <T> Pair<Clearable<T>, Runnable> create(T value) {
        Clearable<T> ret = new Clearable<>(value);
        return Pair.of(ret, () -> ret.value = null);
    }

    public T getValue() {
        return Objects.requireNonNull(value);
    }

    public void ifPresent(NonNullConsumer<T> out) {
        if (value != null) {
            out.accept(value);
        }
    }

    public boolean isPresent() {
        return value != null;
    }
}
