package malte0811.controlengineering.logic.cells;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Pin {
    private final String name;
    private final SignalType type;
    private final PinDirection direction;

    public Pin(String name, SignalType type, PinDirection direction) {
        this.name = name;
        this.type = type;
        this.direction = direction;
    }

    public PinDirection getDirection() {
        return direction;
    }

    public SignalType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static List<Pin> numbered(int numPins, String baseName, SignalType type, PinDirection direction) {
        ImmutableList.Builder<Pin> result = ImmutableList.builder();
        for (int i = 0; i < numPins; ++i) {
            result.add(new Pin(baseName + (i + 1), type, direction));
        }
        return result.build();
    }
}
