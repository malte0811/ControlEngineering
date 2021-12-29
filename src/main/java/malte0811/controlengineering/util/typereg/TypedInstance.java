package malte0811.controlengineering.util.typereg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

// Not "logically" abstract, but the using the class itself causes general issues with the type system
public abstract class TypedInstance<State, Type extends TypedRegistryEntry<State, ?>> {
    private final Type type;
    protected State currentState;

    public TypedInstance(Type type, State currentState) {
        this.type = type;
        this.currentState = currentState;
    }

    public Type getType() {
        return type;
    }

    public State getCurrentState() {
        return currentState;
    }

    protected static <T extends TypedRegistryEntry<?, ? extends I>, I extends TypedInstance<?, ? extends T>>
    Codec<I> makeCodec(TypedRegistry<T> registry) {
        Codec<T> typeCodec = ResourceLocation.CODEC.flatXmap(
                rl -> {
                    T value = registry.get(rl);
                    return value != null ? DataResult.success(value) : DataResult.error("Unknown key: " + rl);
                },
                t -> DataResult.success(t.getRegistryName())
        );
        return typeCodec.dispatch(i -> i.getType(), (TypedRegistryEntry<?, ? extends I> t) -> instanceCodec(t));
    }

    private static <S, I extends TypedInstance<?, ?>>
    Codec<I> instanceCodec(TypedRegistryEntry<S, ? extends I> type) {
        return type.getStateCodec().xmap(state -> type.newInstance(state), i -> (S) i.getCurrentState());
    }
}
