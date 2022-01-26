package malte0811.controlengineering.util.typereg;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import net.minecraft.resources.ResourceLocation;

public abstract class TypedRegistryEntry<StateType, InstanceType extends TypedInstance<StateType, ?>> {
    private final StateType initialState;
    private final MyCodec<StateType> stateCodec;
    private ResourceLocation registryName;

    protected TypedRegistryEntry(StateType initialState, MyCodec<StateType> stateCodec) {
        this.initialState = initialState;
        this.stateCodec = stateCodec;
    }

    public final MyCodec<StateType> getStateCodec() {
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

    public abstract InstanceType newInstance(StateType state);

    public final InstanceType newInstance() {
        return newInstance(getInitialState());
    }
}
