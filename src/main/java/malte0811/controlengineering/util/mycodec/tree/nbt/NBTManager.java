package malte0811.controlengineering.util.mycodec.tree.nbt;

import malte0811.controlengineering.util.mycodec.tree.*;
import net.minecraft.nbt.*;

public class NBTManager implements TreeManager<Tag> {
    public static final TreeManager<Tag> INSTANCE = new NBTManager();

    private NBTManager() {}

    @Override
    public TreeStorageList<Tag> makeList() {
        return new NBTList(new ListTag());
    }

    @Override
    public TreeStorage<Tag> makeTree() {
        return new NBTTree(new CompoundTag());
    }

    @Override
    public TreePrimitive<Tag> makeInt(int value) {
        return new NBTPrimitive(IntTag.valueOf(value));
    }

    @Override
    public TreePrimitive<Tag> makeByte(byte value) {
        return new NBTPrimitive(ByteTag.valueOf(value));
    }

    @Override
    public TreePrimitive<Tag> makeFloat(float value) {
        return new NBTPrimitive(FloatTag.valueOf(value));
    }

    @Override
    public TreePrimitive<Tag> makeDouble(double value) {
        return new NBTPrimitive(DoubleTag.valueOf(value));
    }

    @Override
    public TreePrimitive<Tag> makeString(String value) {
        return new NBTPrimitive(StringTag.valueOf(value));
    }

    @Override
    public TreePrimitive<Tag> makeBoolean(boolean value) {
        return new NBTPrimitive(ByteTag.valueOf(value));
    }

    @Override
    public TreeElement<Tag> makeLong(Long value) {
        return new NBTPrimitive(LongTag.valueOf(value));
    }

    @Override
    public TreeElement<Tag> of(Tag tag) {
        if (tag == null) {
            return new NBTTree(new CompoundTag());
        } else if (tag instanceof ListTag list) {
            return new NBTList(list);
        } else if (tag instanceof CompoundTag tree) {
            return new NBTTree(tree);
        } else {
            return new NBTPrimitive(tag);
        }
    }
}
