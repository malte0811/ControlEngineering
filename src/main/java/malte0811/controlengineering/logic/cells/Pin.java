package malte0811.controlengineering.logic.cells;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public record Pin(SignalType type, PinDirection direction) {

    public static Map<String, Pin> numbered(int numPins, String baseName, SignalType type, PinDirection direction) {
        ImmutableMap.Builder<String, Pin> result = ImmutableMap.builder();
        for (int i = 0; i < numPins; ++i) {
            result.put(baseName + (i + 1), new Pin(type, direction));
        }
        return result.build();
    }
}
