package malte0811.controlengineering.logic.cells;

public enum PinDirection {
    INPUT,
    OUTPUT,
    DELAYED_OUTPUT;

    public boolean isOutput() {
        return this == OUTPUT || this == DELAYED_OUTPUT;
    }

    public boolean isCombinatorialOutput() {
        return this == OUTPUT;
    }
}
