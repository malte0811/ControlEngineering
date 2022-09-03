package malte0811.controlengineering.util.mycodec.tree;

public interface TreePrimitive<Base> extends TreeElement<Base> {
    int asInt();

    boolean asBool();

    byte asByte();

    float asFloat();

    double asDouble();

    String asString();

    long asLong();

    short asShort();
}
