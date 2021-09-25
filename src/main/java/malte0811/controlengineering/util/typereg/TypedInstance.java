package malte0811.controlengineering.util.typereg;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;

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

    DataResult<Dynamic<?>> serializeState() {
        return getType().getStateCodec().encodeStart(NbtOps.INSTANCE, getCurrentState())
                .map(nbt -> new Dynamic<>(NbtOps.INSTANCE, nbt));
    }

    protected static <I extends TypedInstance<?, ?>, T extends TypedRegistryEntry<?>>
    Codec<I> makeCodec(TypedRegistry<T> registry, BiFunction<T, Object, I> makeUncheck) {
        Codec<Pair<ResourceLocation, Dynamic<?>>> baseCodec = RecordCodecBuilder.create(
                inst -> inst.group(
                        ResourceLocation.CODEC.fieldOf("key").forGetter(Pair::getFirst),
                        Codec.PASSTHROUGH.fieldOf("data").forGetter(Pair::getSecond)
                ).apply(inst, Pair::of)
        );
        return baseCodec.flatXmap(
                p -> {
                    T entry = registry.get(p.getFirst());
                    if (entry == null) {
                        return DataResult.error("No such entry");
                    } else {
                        return entry.getStateCodec().decode(p.getSecond())
                                .map(p2 -> makeUncheck.apply(entry, p2.getFirst()));
                    }
                },
                t -> t.serializeState().map(d -> Pair.of(t.getType().getRegistryName(), d))
        );
    }
}
