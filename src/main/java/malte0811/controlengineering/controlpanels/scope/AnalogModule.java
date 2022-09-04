package malte0811.controlengineering.controlpanels.scope;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec4;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AnalogModule extends ScopeModule<AnalogModule.State> {
    public AnalogModule() {
        super(new State(), State.CODEC, 1, false);
    }

    @Nullable
    @Override
    public State enableSomeTrigger(State withoutTrigger) {
        return withoutTrigger.withTriggerChannel(TriggerChannel.LEFT);
    }

    @Override
    public State disableTrigger(State withTrigger) {
        return withTrigger.withTriggerChannel(TriggerChannel.NONE);
    }

    @Override
    public boolean isSomeTriggerEnabled(State state) {
        return state.trigger() != TriggerChannel.NONE;
    }

    public record State(
            boolean risingTrigger, List<TriggerChannel> enabledChannels, boolean moduleEnabled, TriggerChannel trigger
    ) {
        public static final MyCodec<State> CODEC = new RecordCodec4<>(
                MyCodecs.BOOL.fieldOf("rising", State::risingTrigger),
                MyCodecs.list(TriggerChannel.CODEC).fieldOf("enabledChannels", State::enabledChannels),
                MyCodecs.BOOL.fieldOf("enabled", State::moduleEnabled),
                TriggerChannel.CODEC.fieldOf("triggerChannel", State::trigger),
                State::new
        );

        public State() {
            this(true, List.of(TriggerChannel.LEFT, TriggerChannel.RIGHT), true, TriggerChannel.NONE);
        }

        public State withTriggerChannel(TriggerChannel newTrigger) {
            return new State(risingTrigger, enabledChannels, moduleEnabled, newTrigger);
        }

        public State withTriggerSlope(boolean positive) {
            return new State(positive, enabledChannels, moduleEnabled, trigger);
        }

        public State setEnabled(boolean enabled) {
            return new State(risingTrigger, enabledChannels, enabled, trigger);
        }

        public State setChannelEnabled(TriggerChannel channel, boolean enabled) {
            if (isEnabled(channel) == enabled) { return this; }
            List<TriggerChannel> newEnabled = new ArrayList<>(enabledChannels);
            if (enabled) {
                newEnabled.add(channel);
            } else {
                newEnabled.remove(channel);
            }
            return new State(risingTrigger, newEnabled, moduleEnabled, trigger);
        }

        public boolean isEnabled(TriggerChannel channel) {
            return enabledChannels.contains(channel);
        }
    }

    public enum TriggerChannel {
        LEFT, RIGHT, NONE;
        public static final MyCodec<TriggerChannel> CODEC = MyCodecs.forEnum(values(), TriggerChannel::ordinal);
    }
}
