package malte0811.controlengineering.util.mycodec.tree;

public interface TreeStorageList<Base> extends TreeElement<Base>, Iterable<TreeElement<Base>> {
    TreeElement<Base> get(int index);

    void add(TreeElement<Base> newElement);
}
