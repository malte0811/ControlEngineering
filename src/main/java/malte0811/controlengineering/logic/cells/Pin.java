package malte0811.controlengineering.logic.cells;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Pin {
    private final String name;
    private final SignalType type;

    public Pin(String name, SignalType type) {
        this.name = name;
        this.type = type;
    }

    public static List<Pin> numbered(int numPins, String baseName, SignalType type) {
        ImmutableList.Builder<Pin> result = ImmutableList.builder();
        for (int i = 0; i < numPins; ++i) {
            result.add(new Pin(baseName + (i + 1), type));
        }
        return result.build();
    }
}
