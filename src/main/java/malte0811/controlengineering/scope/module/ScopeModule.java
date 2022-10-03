package malte0811.controlengineering.scope.module;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public abstract class ScopeModule<State> extends TypedRegistryEntry<State, ScopeModuleInstance<State>> {
    public static final int VERTICAL_DIV_PIXELS = 10;

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

    // First entry of return value is ignored when isSomeTriggerEnabled is false
    public abstract Pair<Boolean, State> isTriggered(State oldState, BusState input);

    public abstract IntList getActiveTraces(State state);

    public abstract int getNumTraces();

    // Relative to bottom of scope screen
    public abstract double getTraceValueInDivs(int traceId, BusState input, State currentState);

    public final ItemStack getDroppedStack() {
        final var moduleItem = CEItems.SCOPE_MODULES.get(getRegistryName());
        if (moduleItem != null) {
            return moduleItem.get().getDefaultInstance();
        } else {
            return ItemStack.EMPTY;
        }
    }
}
