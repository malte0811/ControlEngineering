package malte0811.controlengineering.logic.clock;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.util.ResourceLocation;

public abstract class ClockGenerator<State> extends TypedRegistryEntry<State> {
    private static final TypedRegistry<ClockGenerator<?>> REGISTRY = new TypedRegistry<>();

    public static <T extends ClockGenerator<?>> T register(ResourceLocation name, T generator) {
        return REGISTRY.register(name, generator);
    }

    protected ClockGenerator(State initialState, Codec<State> stateCodec) {
        super(initialState, stateCodec);
    }

    public abstract boolean shouldTick(State oldState, boolean triggerSignal);

    public abstract State nextState(State oldState, boolean triggerSignal);
}
