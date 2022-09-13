package malte0811.controlengineering.scope;

import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;
import malte0811.controlengineering.util.mycodec.record.RecordCodec4;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return state.trigger().source() != TriggerChannel.NONE;
    }

    public record State(List<ChannelState> channels, boolean moduleEnabled, TriggerState trigger) {
        public static final MyCodec<State> CODEC = new RecordCodec3<>(
                MyCodecs.list(ChannelState.CODEC).fieldOf("channels", State::channels),
                MyCodecs.BOOL.fieldOf("enabled", State::moduleEnabled),
                TriggerState.CODEC.fieldOf("trigger", State::trigger),
                State::new
        );

        public State {
            if (channels.size() != 2) {
                channels = List.of(new ChannelState(), new ChannelState());
            }
        }

        public State() {
            this(List.of(), true, new TriggerState());
        }

        public State withTriggerChannel(TriggerChannel newTrigger) {
            return new State(channels, moduleEnabled, trigger.withChannel(newTrigger));
        }

        public State withTriggerSlope(boolean positive) {
            return new State(channels, moduleEnabled, trigger.withSlope(positive));
        }

        public State withTriggerLevel(int level) {
            return new State(channels, moduleEnabled, trigger.withLevel(level));
        }

        public State setEnabled(boolean enabled) {
            return new State(channels, enabled, trigger);
        }

        public State setChannelEnabled(TriggerChannel channel, boolean enabled) {
            return withChannel(channel, getChannel(channel).withEnable(enabled));
        }

        public State setPerDiv(TriggerChannel channel, int perDiv) {
            return withChannel(channel, getChannel(channel).withPerDiv(perDiv));
        }

        public State setOffset(TriggerChannel channel, int offset) {
            return withChannel(channel, getChannel(channel).withOffset(offset));
        }

        public State setSignalSource(TriggerChannel channel, Optional<BusSignalRef> source) {
            return withChannel(channel, getChannel(channel).withSignalSource(source));
        }

        public ChannelState getChannel(TriggerChannel channel) {
            return channels.get(channel.ordinal());
        }

        public State withChannel(TriggerChannel channel, ChannelState state) {
            List<ChannelState> newChannels = new ArrayList<>(channels());
            newChannels.set(channel.ordinal(), state);
            return new State(newChannels, moduleEnabled, trigger);
        }
    }

    public record TriggerState(boolean risingSlope, TriggerChannel source, int level) {
        public static final MyCodec<TriggerState> CODEC = new RecordCodec3<>(
                MyCodecs.BOOL.fieldOf("rising", TriggerState::risingSlope),
                TriggerChannel.CODEC.fieldOf("source", TriggerState::source),
                MyCodecs.INTEGER.fieldOf("level", TriggerState::level),
                TriggerState::new
        );

        public TriggerState() {
            this(true, TriggerChannel.NONE, 10);
        }

        public TriggerState withChannel(TriggerChannel newSource) {
            return new TriggerState(risingSlope, newSource, level);
        }

        public TriggerState withSlope(boolean positive) {
            return new TriggerState(positive, source, level);
        }

        public TriggerState withLevel(int newLevel) {
            return new TriggerState(risingSlope, source, newLevel);
        }
    }

    public record ChannelState(
            boolean enabled, int perDiv, int zeroOffsetPixels, Optional<BusSignalRef> signal
    ) {
        public static final MyCodec<ChannelState> CODEC = new RecordCodec4<>(
                MyCodecs.BOOL.fieldOf("enabled", ChannelState::enabled),
                MyCodecs.INTEGER.fieldOf("perDiv", ChannelState::perDiv),
                MyCodecs.INTEGER.fieldOf("zeroOffsetPixels", ChannelState::zeroOffsetPixels),
                MyCodecs.optional(BusSignalRef.CODEC).fieldOf("signal", ChannelState::signal),
                ChannelState::new
        );

        public ChannelState() {
            this(true, 128, 50, Optional.empty());
        }

        public ChannelState withEnable(boolean newEnabled) {
            return new ChannelState(newEnabled, perDiv, zeroOffsetPixels, signal);
        }

        public ChannelState withPerDiv(int newPerDiv) {
            return new ChannelState(enabled, newPerDiv, zeroOffsetPixels, signal);
        }

        public ChannelState withOffset(int newOffset) {
            return new ChannelState(enabled, perDiv, newOffset, signal);
        }

        public ChannelState withSignalSource(Optional<BusSignalRef> newSignal) {
            return new ChannelState(enabled, perDiv, zeroOffsetPixels, newSignal);
        }
    }

    public enum TriggerChannel {
        LEFT, RIGHT, NONE;
        public static final MyCodec<TriggerChannel> CODEC = MyCodecs.forEnum(values(), TriggerChannel::ordinal);
    }
}
