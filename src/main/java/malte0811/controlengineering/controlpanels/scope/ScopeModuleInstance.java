package malte0811.controlengineering.controlpanels.scope;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedInstance;

public class ScopeModuleInstance<State> extends TypedInstance<State, ScopeModule<State>> {
    public static final MyCodec<ScopeModuleInstance<?>> CODEC = TypedInstance.makeCodec(ScopeModules.REGISTRY);

    public ScopeModuleInstance(ScopeModule<State> stateScopeModule, State currentState) {
        super(stateScopeModule, currentState);
    }
}
