package malte0811.controlengineering.logic.cells;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class Pin {
    private final SignalType type;
    private final PinDirection direction;

    public Pin(SignalType type, PinDirection direction) {
        this.type = type;
        this.direction = direction;
    }

    public PinDirection getDirection() {
        return direction;
    }

    public SignalType getType() {
        return type;
    }

    public static Map<String, Pin> numbered(int numPins, String baseName, SignalType type, PinDirection direction) {
        ImmutableMap.Builder<String, Pin> result = ImmutableMap.builder();
        for (int i = 0; i < numPins; ++i) {
            result.put(baseName + (i + 1), new Pin(type, direction));
        }
        return result.build();
    }
}
