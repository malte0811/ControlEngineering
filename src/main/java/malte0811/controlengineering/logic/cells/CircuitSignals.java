package malte0811.controlengineering.logic.cells;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import malte0811.controlengineering.bus.BusLine;
import net.minecraft.util.Mth;

import java.util.Map;

public record CircuitSignals(Object2IntMap<String> signals) {
    public static final int MIN_VALID = -BusLine.MAX_VALID_VALUE;
    public static final int MAX_VALID = BusLine.MAX_VALID_VALUE;

    public CircuitSignals {
        boolean modifiable = false;
        for (var entry : signals.object2IntEntrySet()) {
            if (isValid(entry.getIntValue())) {
                continue;
            }
            if (!modifiable) {
                signals = new Object2IntOpenHashMap<>(signals);
                modifiable = true;
            }
            signals.put(entry.getKey(), Mth.clamp(entry.getIntValue(), MIN_VALID, MAX_VALID));
        }
    }

    public static CircuitSignals singleton(String name, int strength) {
        return new CircuitSignals(Object2IntMaps.singleton(name, strength));
    }

    public static CircuitSignals singleton(String name, boolean strength) {
        return new CircuitSignals(Object2IntMaps.singleton(name, debool(strength)));
    }

    public static CircuitSignals of(Map<String, Integer> values) {
        return new CircuitSignals(new Object2IntOpenHashMap<>(values));
    }

    public static CircuitSignals ofBools(Map<String, Boolean> values) {
        var signals = new Object2IntOpenHashMap<String>();
        for (var entry : values.entrySet()) {
            signals.put(entry.getKey(), debool(entry.getValue()));
        }
        return new CircuitSignals(signals);
    }

    public int value(String name) {
        return signals.getInt(name);
    }

    public boolean bool(String name) {
        return bool(value(name));
    }

    public int size() {
        return signals.size();
    }

    public int numTrue() {
        int count = 0;
        for (int signal : signals.values()) {
            if (bool(signal)) {
                count++;
            }
        }
        return count;
    }

    private static int debool(boolean in) {
        return in ? BusLine.MAX_VALID_VALUE : BusLine.MIN_VALID_VALUE;
    }

    private static boolean bool(int in) {
        return in > 0;
    }

    public ObjectSet<Object2IntMap.Entry<String>> entries() {
        return signals.object2IntEntrySet();
    }

    private static boolean isValid(int strength) {
        return strength >= MIN_VALID && strength <= MAX_VALID;
    }
}
