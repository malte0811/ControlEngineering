package malte0811.controlengineering.logic.cells;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.nbt.CompoundNBT;

public final class LeafcellInstance<State> {
    private final LeafcellType<State> type;
    private State currentState;

    public LeafcellInstance(LeafcellType<State> type, State currentState) {
        this.type = type;
        this.currentState = currentState;
    }

    public CompoundNBT toNBT() {
        return type.toNBT(currentState);
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
}
