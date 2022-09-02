package malte0811.controlengineering.controlpanels.scope;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;

public abstract class ScopeModule<State> extends TypedRegistryEntry<State, ScopeModuleInstance<State>> {
    public ScopeModule(State initialState, MyCodec<State> stateCodec) {
        super(initialState, stateCodec);
    }

    @Override
    public ScopeModuleInstance<State> newInstance(State state) {
        return new ScopeModuleInstance<>(this, state);
    }

    public abstract int getWidth();
}
