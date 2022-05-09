package malte0811.controlengineering.controlpanels;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.serial.PacketBufferStorage;
import malte0811.controlengineering.util.typereg.TypedInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public final class PanelComponentInstance<Config, State> extends TypedInstance<Pair<Config, State>, PanelComponentType<Config, State>> {
    public static final MyCodec<PanelComponentInstance<?, ?>> CODEC = TypedInstance.makeCodec(PanelComponents.REGISTRY);

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

    public InteractionResult onClick(PanelComponentType.ComponentClickContext ctx, boolean isClient) {
        Pair<InteractionResult, State> clickResult = getType().click(getConfig(), getState(), ctx);
        if (!isClient) {
            currentState = Pair.of(getConfig(), clickResult.getSecond());
        }
        return clickResult.getFirst();
    }

    public TickResult tick() {
        final var oldState = currentState;
        currentState = Pair.of(getConfig(), getType().tick(getConfig(), getState()));
        return new TickResult(
                !Objects.equals(oldState, currentState),
                getType().canClientDistinguish(oldState.getSecond(), currentState.getSecond())
        );
    }

    public BusState getEmittedState() {
        return getType().getEmittedState(getConfig(), getState());
    }

    public Config getConfig() {
        return currentState.getFirst();
    }

    public void writeToWithoutState(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(getType().getRegistryName());
        getType().getConfigCodec().toSerial(new PacketBufferStorage(buffer), getConfig());
    }

    public State getState() {
        return currentState.getSecond();
    }

    public void updateTotalState(BusState totalState) {
        State newState = getType().updateTotalState(getConfig(), getState(), totalState);
        currentState = Pair.of(getConfig(), newState);
    }

    public PanelComponentInstance<?, ?> copy(boolean clearState) {
        var stateToUse = currentState.getSecond();
        if (clearState) {
            stateToUse = getType().updateTotalState(getConfig(), stateToUse, BusState.EMPTY);
        }
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

    public Vec2d getSize(Level level) {
        return getType().getSize(getConfig(), level);
    }

    @Override
    public String toString() {
        return "type=" + getType() + ";config=" + getConfig() + ";state=" + getState();
    }

    public AABB getSelectionShape() {
        return getType().getSelectionShape(getState());
    }

    public record TickResult(boolean updateBus, boolean updateClient) {}
}
