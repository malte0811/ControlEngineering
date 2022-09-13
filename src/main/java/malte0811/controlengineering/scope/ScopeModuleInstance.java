package malte0811.controlengineering.scope;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedInstance;

import java.util.List;

// TODO split state into config and measurements
public class ScopeModuleInstance<State> extends TypedInstance<State, ScopeModule<State>> {
    public static final MyCodec<ScopeModuleInstance<?>> CODEC = TypedInstance.makeCodec(ScopeModules.REGISTRY);

    public ScopeModuleInstance(ScopeModule<State> stateScopeModule, State currentState) {
        super(stateScopeModule, currentState);
    }

    public void setConfig(State newConfig) {
        currentState = newConfig;
    }

    public boolean triggerActive() {
        return getType().isSomeTriggerEnabled(getCurrentState());
    }

    public boolean enableSomeTrigger() {
        final var newState = getType().enableSomeTrigger(getCurrentState());
        if (newState != null) {
            currentState = newState;
            return true;
        } else {
            return false;
        }
    }

    public void disableTrigger() {
        currentState = getType().disableTrigger(getCurrentState());
    }

    public static void ensureOneTriggerActive(List<ScopeModuleInstance<?>> modules) {
        if (modules.stream().noneMatch(ScopeModuleInstance::triggerActive)) {
            for (final var module : modules) {
                if (module.enableSomeTrigger()) {
                    return;
                }
            }
        }
    }
}
