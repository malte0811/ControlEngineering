package malte0811.controlengineering.util.mycodec.tree;

public interface TreeStorage<Base> extends TreeElement<Base> {
    void put(String key, TreeElement<Base> element);

    TreeElement<Base> get(String key);
}
