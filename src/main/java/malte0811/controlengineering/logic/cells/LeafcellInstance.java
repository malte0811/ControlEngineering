package malte0811.controlengineering.logic.cells;

public final class LeafcellInstance<State> {
    private final LeafcellType<State> type;
    private State currentState;

    public LeafcellInstance(LeafcellType<State> type, State currentState) {
        this.type = type;
        this.currentState = currentState;
    }
}
