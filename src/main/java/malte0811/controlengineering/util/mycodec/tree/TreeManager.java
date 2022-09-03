package malte0811.controlengineering.util.mycodec.tree;

public interface TreeManager<Base> {
    TreeStorageList<Base> makeList();

    TreeStorage<Base> makeTree();

    TreePrimitive<Base> makeInt(int value);

    TreePrimitive<Base> makeByte(byte value);

    TreePrimitive<Base> makeFloat(float value);

    TreePrimitive<Base> makeDouble(double value);

    TreePrimitive<Base> makeString(String value);

    TreePrimitive<Base> makeBoolean(boolean value);

    TreeElement<Base> makeLong(long value);

    TreeElement<Base> of(Base base);

    TreeElement<Base> makeShort(short in);
}
