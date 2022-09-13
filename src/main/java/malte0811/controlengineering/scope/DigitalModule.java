package malte0811.controlengineering.scope;

import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;
import malte0811.controlengineering.util.mycodec.record.RecordCodec4;

import java.util.ArrayList;
import java.util.List;

public class DigitalModule extends ScopeModule<DigitalModule.State> {
    public static final int NO_LINE = -1;

    public DigitalModule() {
        super(new State(), State.CODEC, 2, false);
    }

    public enum TriggerState {
        LOW, IGNORED, HIGH;

        public static final MyCodec<TriggerState> CODEC = MyCodecs.INTEGER
                .xmap(i -> TriggerState.values()[i], TriggerState::ordinal);
    }

    @Override
    public State enableSomeTrigger(State withoutTrigger) {
        return withoutTrigger.withTrigger(true);
    }

    @Override
    public State disableTrigger(State withTrigger) {
        return withTrigger.withTrigger(false);
    }

    @Override
    public boolean isSomeTriggerEnabled(State state) {
        return state.inputState.triggerEnabled();
    }

    public record State(InputState inputState, boolean moduleEnabled, int verticalOffset) {
        public static final MyCodec<State> CODEC = new RecordCodec3<>(
                InputState.CODEC.fieldOf("inputState", State::inputState),
                MyCodecs.BOOL.fieldOf("moduleEnabled", State::moduleEnabled),
                MyCodecs.INTEGER.fieldOf("verticalOffset", State::verticalOffset),
                State::new
        );

        public State() {
            this(new InputState(), true, 50);
        }

        public State withTrigger(int channel, TriggerState newState) {
            return withInputState(inputState.withTrigger(channel, newState));
        }

        public State toggleChannel(int id) {
            return withInputState(inputState.toggleChannel(id));
        }

        public State toggleModule() {
            return new State(inputState, !moduleEnabled, verticalOffset);
        }

        public State withTrigger(boolean enable) {
            return withInputState(inputState.withTrigger(enable));
        }

        public State withOffset(int offset) {
            return new State(inputState, moduleEnabled, offset);
        }

        private State withInputState(InputState newInputState) {
            return new State(newInputState, moduleEnabled, verticalOffset);
        }

        public State withInput(int line) {
            return withInputState(inputState.withInput(line));
        }
    }

    public record InputState(
            List<TriggerState> channelTriggers,
            short enabledChannelsMask,
            boolean triggerEnabled,
            int inputLine
    ) {
        public static final MyCodec<InputState> CODEC = new RecordCodec4<>(
                MyCodecs.list(TriggerState.CODEC).fieldOf("triggerStates", InputState::channelTriggers),
                MyCodecs.SHORT.fieldOf("enableMask", InputState::enabledChannelsMask),
                MyCodecs.BOOL.fieldOf("triggerEnabled", InputState::triggerEnabled),
                MyCodecs.INTEGER.fieldOf("inputLine", InputState::inputLine),
                InputState::new
        );

        public InputState {
            while (channelTriggers.size() < BusLine.LINE_SIZE) {
                channelTriggers.add(TriggerState.IGNORED);
            }
        }

        public InputState() {
            this(new ArrayList<>(), (short) -1, false, NO_LINE);
        }

        public boolean isChannelVisible(int id) {
            return (enabledChannelsMask & (1 << id)) != 0;
        }

        public InputState withTrigger(int channel, TriggerState newState) {
            final var newTriggers = new ArrayList<>(channelTriggers);
            newTriggers.set(channel, newState);
            return new InputState(newTriggers, enabledChannelsMask, triggerEnabled, inputLine);
        }

        public InputState toggleChannel(int id) {
            final int newMask = enabledChannelsMask ^ (1 << id);
            return new InputState(channelTriggers, (short) newMask, triggerEnabled, inputLine);
        }

        public InputState withTrigger(boolean enable) {
            return new InputState(channelTriggers, enabledChannelsMask, enable, inputLine);
        }

        public InputState withInput(int line) {
            return new InputState(channelTriggers, enabledChannelsMask, triggerEnabled, line);
        }
    }
}
