package malte0811.controlengineering.controlpanels;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.typereg.TypedInstance;
import net.minecraft.util.ActionResultType;

import java.util.Objects;

public final class PanelComponentInstance<Config, State> extends TypedInstance<Pair<Config, State>, PanelComponentType<Config, State>> {
    public static final Codec<PanelComponentInstance<?, ?>> CODEC = TypedInstance.makeCodec(
            PanelComponents.REGISTRY, PanelComponentInstance::makeUnchecked
    );

    public PanelComponentInstance(PanelComponentType<Config, State> type, Pair<Config, State> state) {
        super(type, state);
    }

    public ActionResultType onClick() {
        Pair<ActionResultType, State> clickResult = getType().click(getConfig(), getState());
        currentState = Pair.of(getConfig(), clickResult.getSecond());
        return clickResult.getFirst();
    }

    public BusState getEmittedState() {
        return getType().getEmittedState(getConfig(), getState());
    }

    public Config getConfig() {
        return currentState.getFirst();
    }

    public State getState() {
        return currentState.getSecond();
    }

    public void updateTotalState(BusState totalState) {
        State newState = getType().updateTotalState(getConfig(), getState(), totalState);
        currentState = Pair.of(getConfig(), newState);
    }

    private static <Config, State>
    PanelComponentInstance<Config, State> makeUnchecked(PanelComponentType<Config, State> type, Object state) {
        return new PanelComponentInstance<>(type, (Pair<Config, State>) state);
    }

    public PanelComponentInstance<?, ?> copy() {
        return new PanelComponentInstance<>(getType(), getCurrentState());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PanelComponentInstance<?, ?>)) {
            return false;
        }
        PanelComponentInstance<?, ?> inst = (PanelComponentInstance<?, ?>) obj;
        return getType() == inst.getType() &&
                getConfig().equals(inst.getConfig()) &&
                getState().equals(inst.getState());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getConfig(), getState());
    }
}
