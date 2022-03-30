package malte0811.controlengineering.util.mycodec.tree.nbt;

import malte0811.controlengineering.util.mycodec.tree.TreePrimitive;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.Objects;

public class NBTPrimitive extends NBTElement<Tag> implements TreePrimitive<Tag> {
    public NBTPrimitive(Tag value) {
        super(value);
    }

    @Override
    public int asInt() {
        return getNumeric().getAsInt();
    }

    @Override
    public boolean asBool() {
        return getNumeric().getAsInt() != 0;
    }

    @Override
    public byte asByte() {
        return getNumeric().getAsByte();
    }

    @Override
    public float asFloat() {
        return getNumeric().getAsFloat();
    }

    @Override
    public double asDouble() {
        return getNumeric().getAsDouble();
    }

    @Override
    public String asString() {
        return Objects.requireNonNullElseGet(getDirect(), () -> StringTag.valueOf("")).getAsString();
    }

    private NumericTag getNumeric() {
        return getDirect() instanceof NumericTag numeric ? numeric : IntTag.valueOf(0);
    }
}
