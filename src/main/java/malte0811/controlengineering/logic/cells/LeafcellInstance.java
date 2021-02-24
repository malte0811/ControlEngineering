package malte0811.controlengineering.logic.cells;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import malte0811.controlengineering.util.typereg.TypedInstance;
import net.minecraft.nbt.CompoundNBT;

public final class LeafcellInstance<State> extends TypedInstance<State, LeafcellType<State>> {
    private final LeafcellType<State> type;
    private State currentState;

    public LeafcellInstance(LeafcellType<State> type, State currentState) {
        super(type, currentState);
        this.type = type;
        this.currentState = currentState;
    }

    public LeafcellType<State> getType() {
        return type;
    }

    public State getCurrentState() {
        return currentState;
    }

    public DoubleList tick(DoubleList inputValues) {
        currentState = type.nextState(inputValues, currentState);
        return type.getOutputSignals(inputValues, currentState);
    }

    public static LeafcellInstance<?> fromNBT(CompoundNBT nbt) {
        return fromNBT(nbt, LeafcellType.REGISTRY, LeafcellInstance::makeUnchecked);
    }

    private static <T> LeafcellInstance<T> makeUnchecked(LeafcellType<T> type, Object state) {
        return new LeafcellInstance<>(type, (T) state);
    }
}
