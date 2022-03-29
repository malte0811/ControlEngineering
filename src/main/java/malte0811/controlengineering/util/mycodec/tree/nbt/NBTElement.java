package malte0811.controlengineering.util.mycodec.tree.nbt;

import malte0811.controlengineering.util.mycodec.tree.TreeElement;
import net.minecraft.nbt.Tag;

public abstract class NBTElement<T extends Tag> implements TreeElement<Tag> {
    private final T value;

    protected NBTElement(T value) {
        this.value = value;
    }

    @Override
    public T getDirect() {
        return value;
    }
}
