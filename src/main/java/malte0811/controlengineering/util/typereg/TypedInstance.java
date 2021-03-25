package malte0811.controlengineering.util.typereg;

import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiFunction;

// Not "logically" abstract, but the using the class itself causes general issues with the type system
public abstract class TypedInstance<State, Type extends TypedRegistryEntry<State>> {
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

    public final CompoundNBT toNBT() {
        CompoundNBT result = new CompoundNBT();
        INBT stateNBT = Codecs.encode(type.getStateCodec(), currentState);
        result.put("state", stateNBT);
        result.putString("type", type.getRegistryName().toString());
        return result;
    }

    @Nullable
    protected static <T extends TypedRegistryEntry<?>, I extends TypedInstance<?, ? extends T>>
    I fromNBT(CompoundNBT nbt, TypedRegistry<T> registry, BiFunction<T, Object, I> make) {
        if (nbt == null) {
            return null;
        }
        ResourceLocation name = ResourceLocation.tryCreate(nbt.getString("type"));
        if (name == null) {
            return null;
        }
        T type = registry.get(name);
        if (type == null) {
            return null;
        }
        Optional<?> state = Codecs.read(type.getStateCodec(), nbt.get("state")).result();
        if (state.isPresent()) {
            return make.apply(type, state.get());
        } else {
            return null;
        }
    }
}
