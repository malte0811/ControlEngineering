package malte0811.controlengineering.scope.module;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.bus.BusState;
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

    public boolean checkTriggered(BusState input) {
        final var result = getType().isTriggered(getCurrentState(), input);
        final var triggerEnabled = getType().isSomeTriggerEnabled(getCurrentState());
        currentState = result.getSecond();
        return triggerEnabled && result.getFirst();
    }

    public double getDivRelativeSample(int trace, BusState input) {
        return getType().getTraceValueInDivs(trace, input, getCurrentState());
    }

    public IntList getActiveTraces() {
        return getType().getActiveTraces(currentState);
    }

    public static void ensureOneTriggerActive(List<ScopeBlockEntity.ModuleInScope> modules, int preferredTrigger) {
        final IntList triggerIndices = new IntArrayList();
        boolean isPreferredTrigger = false;
        for (int i = 0; i < modules.size(); ++i) {
            final var module = modules.get(i).module();
            if (module.isEnabled() && module.triggerActive()) {
                triggerIndices.add(i);
                isPreferredTrigger |= i == preferredTrigger;
            } else if (!module.isEnabled()) {
                module.disableTrigger();
            }
        }
        if (triggerIndices.isEmpty()) {
            for (final var module : modules) {
                if (module.module().isEnabled() && module.module().enableSomeTrigger()) {
                    return;
                }
            }
        } else if (triggerIndices.size() > 1) {
            final int chosenTrigger = isPreferredTrigger ? preferredTrigger : triggerIndices.getInt(0);
            for (int i : triggerIndices) {
                if (i != chosenTrigger) {
                    modules.get(i).module().disableTrigger();
                }
            }
        }
    }

    public int getPowerConsumption() {
        return getType().getModulePowerConsumption(getCurrentState());
    }

    public boolean isEnabled() {
        return getType().isEnabled(getCurrentState());
    }
}
