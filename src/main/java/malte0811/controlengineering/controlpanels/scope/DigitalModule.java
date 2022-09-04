package malte0811.controlengineering.controlpanels.scope;

import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec4;

import java.util.ArrayList;
import java.util.List;

public class DigitalModule extends ScopeModule<DigitalModule.State> {
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
        return state.triggerEnabled();
    }

    public record State(
            List<TriggerState> channelTriggers,
            short enabledChannelsMask,
            boolean moduleEnabled,
            boolean triggerEnabled
    ) {
        public static final MyCodec<State> CODEC = new RecordCodec4<>(
                MyCodecs.list(TriggerState.CODEC).fieldOf("triggerStates", State::channelTriggers),
                MyCodecs.SHORT.fieldOf("enableMask", State::enabledChannelsMask),
                MyCodecs.BOOL.fieldOf("moduleEnabled", State::moduleEnabled),
                MyCodecs.BOOL.fieldOf("triggerEnabled", State::triggerEnabled),
                State::new
        );

        public State {
            while (channelTriggers.size() < BusLine.LINE_SIZE) {
                channelTriggers.add(TriggerState.IGNORED);
            }
        }

        public State() {
            this(new ArrayList<>(), (short) -1, true, false);
        }

        public State withTrigger(int channel, TriggerState newState) {
            final var newTriggers = new ArrayList<>(channelTriggers);
            newTriggers.set(channel, newState);
            return new State(newTriggers, enabledChannelsMask, moduleEnabled(), triggerEnabled);
        }

        public boolean isChannelVisible(int id) {
            return (enabledChannelsMask & (1 << id)) != 0;
        }

        public State toggleChannel(int id) {
            final int newMask = enabledChannelsMask ^ (1 << id);
            return new State(channelTriggers, (short) newMask, moduleEnabled(), triggerEnabled);
        }

        public State toggleModule() {
            return new State(channelTriggers, enabledChannelsMask, !moduleEnabled, triggerEnabled);
        }

        public State withTrigger(boolean enable) {
            return new State(channelTriggers, enabledChannelsMask, moduleEnabled, enable);
        }
    }
}
