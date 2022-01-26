package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;

public class CoveredToggleSwitch extends PanelComponentType<ColorAndSignal, CoveredToggleSwitch.State> {
    public CoveredToggleSwitch() {
        super(
                new ColorAndSignal(0xff0000, BusSignalRef.DEFAULT), State.CLOSED,
                ColorAndSignal.CODEC, State.CODEC,
                ToggleSwitch.SIZE, ToggleSwitch.SELECTION_HEIGHT
        );
    }

    @Override
    public BusState getEmittedState(ColorAndSignal config, State state) {
        if (state == State.ACTIVE) {
            return config.signal().singleSignalState(BusLine.MAX_VALID_VALUE);
        } else {
            return BusState.EMPTY;
        }
    }

    @Override
    public Pair<InteractionResult, State> click(
            ColorAndSignal config, State oldState, boolean sneaking, Vec3 relativeHit
    ) {
        if (sneaking) {
            return Pair.of(oldState == State.CLOSED ? InteractionResult.PASS : InteractionResult.SUCCESS, State.CLOSED);
        } else {
            var newState = switch (oldState) {
                case CLOSED -> State.OPEN;
                case OPEN -> State.ACTIVE;
                case ACTIVE -> State.CLOSED;
            };
            return Pair.of(InteractionResult.SUCCESS, newState);
        }
    }

    public enum State {
        CLOSED, OPEN, ACTIVE;
        private static final State[] STATES = values();
        public static final MyCodec<State> CODEC = MyCodecs.INTEGER.xmap(i -> STATES[i], State::ordinal);
    }
}
