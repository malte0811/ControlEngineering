package malte0811.controlengineering.util.mycodec.tree.nbt;

import malte0811.controlengineering.util.mycodec.tree.TreeElement;
import malte0811.controlengineering.util.mycodec.tree.TreeStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class NBTTree extends NBTElement<CompoundTag> implements TreeStorage<Tag> {
    public NBTTree(CompoundTag value) {
        super(value);
    }

    @Override
    public void put(String key, TreeElement<Tag> element) {
        getDirect().put(key, element.getDirect());
    }

    @Override
    public TreeElement<Tag> get(String key) {
        return NBTManager.INSTANCE.of(getDirect().get(key));
    }
}
