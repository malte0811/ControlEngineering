package malte0811.controlengineering.util.serialization;

import net.minecraft.nbt.INBT;

import java.util.function.Function;

public class StringSerializableField<Owner, Type> {
    private final String name;
    private final StringSerializableCodec<Type> codec;
    private final Function<Owner, Type> get;

    public StringSerializableField(String name, StringSerializableCodec<Type> codec, Function<Owner, Type> get) {
        this.name = name;
        this.codec = codec;
        this.get = get;
    }

    public String getName() {
        return name;
    }

    public StringSerializableCodec<Type> getCodec() {
        return codec;
    }

    public Type getValue(Owner from) {
        return get.apply(from);
    }

    public INBT toNBT(Owner input) {
        return codec.toNBT(get.apply(input));
    }
}
