package malte0811.controlengineering.bus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class BusEmitterCombiner<T> {
    private final Function<T, BusState> getEmittedState;
    private final Consumer<T> updateTotalState;

    private final Map<T, BusState> outputByBlock = new HashMap<>();
    private BusState totalState = BusState.EMPTY;
    private BusState totalEmittedState = BusState.EMPTY;

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
            totalEmittedState = totalEmittedState.merge(emittedState);
        }
        totalState = totalEmittedState.merge(initialState);
        if (!oldState.equals(Objects.requireNonNull(totalState))) {
            for (T entry : outputByBlock.keySet()) {
                updateTotalState.accept(entry);
            }
        }
    }

    public BusState getTotalState() {
        return totalState;
    }

    public BusState getTotalEmittedState() {
        return totalEmittedState;
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
    }
}
