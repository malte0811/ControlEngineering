package malte0811.controlengineering.util.typereg;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public abstract class TypedRegistryEntry<StateType> {
    private final StateType initialState;
    private final Codec<StateType> stateCodec;
    private ResourceLocation registryName;

    protected TypedRegistryEntry(StateType initialState, Codec<StateType> stateCodec) {
        this.initialState = initialState;
        this.stateCodec = stateCodec;
    }

    public final Codec<StateType> getStateCodec() {
        return stateCodec;
    }

    public final StateType getInitialState() {
        return initialState;
    }

    public final ResourceLocation getRegistryName() {
        return Preconditions.checkNotNull(this.registryName);
    }

    public final void setRegistryName(ResourceLocation registryName) {
        Preconditions.checkState(this.registryName == null);
        this.registryName = registryName;
    }

    public abstract TypedInstance<StateType, ? extends TypedRegistryEntry<StateType>> newInstance();
}
