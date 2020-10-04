package malte0811.controlengineering.bus;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class BusEmitterCombiner<T> {
    private final Function<T, BusState> getEmittedState;
    private final Consumer<T> updateTotalState;

    private Map<T, BusState> outputByBlock = new HashMap<>();
    private BusState totalState = new BusState();
    private BusState totalEmittedState = new BusState();

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

    public void removeEmitter(T emitter) {
        outputByBlock.remove(emitter);
    }

    public void updateState(BusState initialState) {
        final BusState oldState = totalState;
        totalEmittedState = new BusState();
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

    public Iterable<T> getEmitters() {
        return outputByBlock.keySet();
    }

    public void clear() {
        outputByBlock.clear();
    }
}