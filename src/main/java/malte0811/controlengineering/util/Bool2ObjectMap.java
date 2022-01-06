package malte0811.controlengineering.util;

public class Bool2ObjectMap<V> {
    private V forTrue;
    private V forFalse;

    public void put(boolean key, V value) {
        if (key) {
            forTrue = value;
        } else {
            forFalse = value;
        }
    }

    public V get(boolean key) {
        if (key) {
            return forTrue;
        } else {
            return forFalse;
        }
    }
}
