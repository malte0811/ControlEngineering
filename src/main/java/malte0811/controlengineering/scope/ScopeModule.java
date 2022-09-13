package malte0811.controlengineering.scope;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;

import javax.annotation.Nullable;

public abstract class ScopeModule<State> extends TypedRegistryEntry<State, ScopeModuleInstance<State>> {
    private final int width;
    private final boolean empty;

    public ScopeModule(State initialState, MyCodec<State> stateCodec, int width, boolean empty) {
        super(initialState, stateCodec);
        this.width = width;
        this.empty = empty;
    }

    @Override
    public ScopeModuleInstance<State> newInstance(State state) {
        return new ScopeModuleInstance<>(this, state);
    }

    public final int getWidth() {
        return width;
    }

    public final boolean isEmpty() {
        return empty;
    }

    @Nullable
    public abstract State enableSomeTrigger(State withoutTrigger);

    public abstract State disableTrigger(State withTrigger);

    public abstract boolean isSomeTriggerEnabled(State state);
}
