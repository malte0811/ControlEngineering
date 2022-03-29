package malte0811.controlengineering.util.typereg;

import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;

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
    MyCodec<I> makeCodec(TypedRegistry<T> registry) {
        MyCodec<T> typeCodec = MyCodecs.RESOURCE_LOCATION.flatXmap(
                rl -> {
                    T value = registry.get(rl);
                    return value != null ? FastDataResult.success(value) : FastDataResult.error("Unknown key: " + rl);
                },
                T::getRegistryName
        );
        return typeCodec.dispatch(I::getType, (TypedRegistryEntry<?, ? extends I> t) -> instanceCodec(t));
    }

    private static <S, I extends TypedInstance<?, ?>>
    MyCodec<I> instanceCodec(TypedRegistryEntry<S, ? extends I> type) {
        return type.getStateCodec().xmap(state -> type.newInstance(state), i -> (S) i.getCurrentState());
    }
}
