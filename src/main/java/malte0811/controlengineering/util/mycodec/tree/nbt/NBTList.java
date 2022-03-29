package malte0811.controlengineering.util.mycodec.tree.nbt;

import malte0811.controlengineering.util.mycodec.tree.TreeElement;
import malte0811.controlengineering.util.mycodec.tree.TreeStorageList;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class NBTList extends NBTElement<ListTag> implements TreeStorageList<Tag> {
    public NBTList(ListTag value) {
        super(value);
    }

    @Override
    public TreeElement<Tag> get(int index) {
        return NBTManager.INSTANCE.of(getDirect().get(index));
    }

    @Override
    public void add(TreeElement<Tag> newElement) {
        getDirect().add(newElement.getDirect());
    }

    @Nonnull
    @Override
    public Iterator<TreeElement<Tag>> iterator() {
        return getDirect().stream()
                .map(NBTManager.INSTANCE::of)
                .iterator();
    }
}
