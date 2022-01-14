package malte0811.controlengineering.bus;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class BusEmitterCombiner<T> {
    private final Function<T, BusState> getEmittedState;
    private final Consumer<T> updateTotalState;

    private final Map<T, BusState> outputByBlock = new HashMap<>();
    @Nullable
    private BusState totalState = null;
    @Nullable
    private BusState totalEmittedState = null;

    public BusEmitterCombiner(
            Function<T, BusState> getEmittedState,
            Consumer<T> updateTotalState
    ) {
        this.getEmittedState = getEmittedState;
        this.updateTotalState = updateTotalState;
    }

    public void addEmitter(T emitter) {
        outputByBlock.put(emitter, getEmittedState.apply(emitter));
    }

    public void removeEmitterIfPresent(T emitter) {
        outputByBlock.remove(emitter);
    }

    public void updateState(BusState initialState) {
        final BusState oldState = totalState;
        totalEmittedState = BusState.EMPTY;
        for (T key : outputByBlock.keySet()) {
            final BusState emittedState = getEmittedState.apply(key);
            outputByBlock.put(key, emittedState);
            totalEmittedState = Objects.requireNonNull(totalEmittedState).merge(emittedState);
        }
        totalState = totalEmittedState.merge(initialState);
        if (oldState == null || !oldState.equals(totalState)) {
            for (T entry : outputByBlock.keySet()) {
                updateTotalState.accept(entry);
            }
        }
    }

    public BusState getTotalState() {
        return Objects.requireNonNullElse(totalState, BusState.EMPTY);
    }

    public BusState getTotalEmittedState() {
        return Objects.requireNonNullElse(totalEmittedState, BusState.EMPTY);
    }

    public BusState getStateWithout(T excluded) {
        BusState merged = BusState.EMPTY;
        for (Map.Entry<T, BusState> entry : outputByBlock.entrySet()) {
            if (!excluded.equals(entry.getKey())) {
                merged = merged.merge(entry.getValue());
            }
        }
        return merged;
    }

    public Iterable<T> getEmitters() {
        return outputByBlock.keySet();
    }

    public void clear() {
        outputByBlock.clear();
        totalState = totalEmittedState = null;
    }
}
