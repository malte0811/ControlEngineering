package malte0811.controlengineering.controlpanels;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.typereg.TypedInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public final class PanelComponentInstance<Config, State> extends TypedInstance<Pair<Config, State>, PanelComponentType<Config, State>> {
    public static final Codec<PanelComponentInstance<?, ?>> CODEC = TypedInstance.makeCodec(
            PanelComponents.REGISTRY, PanelComponentInstance::makeUnchecked
    );

    public PanelComponentInstance(PanelComponentType<Config, State> type, Pair<Config, State> state) {
        super(type, state);
    }

    public PanelComponentInstance(PanelComponentType<Config, State> type, Config config, State state) {
        super(type, Pair.of(config, state));
    }

    @Nullable
    public static PanelComponentInstance<?, ?> readFrom(FriendlyByteBuf buffer) {
        ResourceLocation typeName;
        try {
            typeName = buffer.readResourceLocation();
        } catch (Exception x) {
            return null;
        }
        PanelComponentType<?, ?> type = PanelComponents.REGISTRY.get(typeName);
        if (type == null) {
            return null;
        }
        return type.newInstance(buffer);
    }

    public InteractionResult onClick() {
        Pair<InteractionResult, State> clickResult = getType().click(getConfig(), getState());
        currentState = Pair.of(getConfig(), clickResult.getSecond());
        return clickResult.getFirst();
    }

    public BusState getEmittedState() {
        return getType().getEmittedState(getConfig(), getState());
    }

    public Config getConfig() {
        return currentState.getFirst();
    }

    public void writeToWithoutState(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(getType().getRegistryName());
        getType().getConfigParser().writeToBuffer(getConfig(), buffer);
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

    public PanelComponentInstance<?, ?> copy(boolean clearState) {
        State stateToUse = clearState ? getType().getInitialState().getSecond() : getState();
        return new PanelComponentInstance<>(getType(), Pair.of(getConfig(), stateToUse));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PanelComponentInstance<?, ?> inst)) {
            return false;
        }
        return getType() == inst.getType() &&
                getConfig().equals(inst.getConfig()) &&
                getState().equals(inst.getState());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getConfig(), getState());
    }

    public List<String> toCNCStrings() {
        return getType().toCNCStrings(getConfig());
    }

    public void setConfig(Config newConfig) {
        currentState = Pair.of(newConfig, getState());
    }

    public Vec2d getSize() {
        return getType().getSize(getConfig());
    }

    @Override
    public String toString() {
        return "type=" + getType() + ";config=" + getConfig() + ";state=" + getState();
    }
}
