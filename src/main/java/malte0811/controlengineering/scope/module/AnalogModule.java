package malte0811.controlengineering.scope.module;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;
import malte0811.controlengineering.util.mycodec.record.RecordCodec5;

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

    @Override
    public Pair<Boolean, State> isTriggered(State oldState, BusState input) {
        List<ChannelState> newChannels = new ArrayList<>(2);
        for (int i = 0; i < 2; ++i) {
            final var currentInput = getInputBusSignal(input, oldState, i);
            newChannels.add(oldState.channels.get(i).withLastInput(currentInput));
        }
        State newState = new State(newChannels, oldState.moduleEnabled(), oldState.trigger());
        final int sourceIndex = oldState.trigger().source() == TriggerChannel.RIGHT ? 1 : 0;
        final var threshold = oldState.trigger.level;
        final var oldAbove = oldState.channels.get(sourceIndex).lastSignal > threshold;
        final var newAbove = newState.channels.get(sourceIndex).lastSignal > threshold;
        final var triggered = newAbove == oldState.trigger.risingSlope && oldAbove != newAbove;
        return Pair.of(triggered, newState);
    }

    @Override
    public IntList getActiveTraces(State state) {
        final var active = new IntArrayList();
        for (int i = 0; i < getNumTraces(); ++i) {
            if (state.channels.get(i).enabled) {
                active.add(i);
            }
        }
        return active;
    }

    @Override
    public int getNumTraces() {
        return 2;
    }

    @Override
    public double getTraceValueInDivs(int traceId, BusState input, State currentState) {
        final var channelCfg = currentState.channels.get(traceId);
        final var baseOffset = channelCfg.zeroOffsetPixels / (double) VERTICAL_DIV_PIXELS;
        final var signalHeight = getInputBusSignal(input, currentState, traceId) / (double) channelCfg.perDiv;
        return baseOffset + signalHeight;
    }

    private int getInputBusSignal(BusState input, State state, int channel) {
        final var channelConfig = state.channels.get(channel);
        return channelConfig.signal.map(input::getSignal).orElse(0);
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
            boolean enabled, int perDiv, int zeroOffsetPixels, Optional<BusSignalRef> signal, int lastSignal
    ) {
        public static final MyCodec<ChannelState> CODEC = new RecordCodec5<>(
                MyCodecs.BOOL.fieldOf("enabled", ChannelState::enabled),
                MyCodecs.INTEGER.fieldOf("perDiv", ChannelState::perDiv),
                MyCodecs.INTEGER.fieldOf("zeroOffsetPixels", ChannelState::zeroOffsetPixels),
                MyCodecs.optional(BusSignalRef.CODEC).fieldOf("signal", ChannelState::signal),
                MyCodecs.INTEGER.fieldOf("lastSignal", ChannelState::lastSignal),
                ChannelState::new
        );

        public ChannelState() {
            this(true, 128, 50, Optional.empty(), 0);
        }

        public ChannelState withEnable(boolean newEnabled) {
            return new ChannelState(newEnabled, perDiv, zeroOffsetPixels, signal, lastSignal);
        }

        public ChannelState withPerDiv(int newPerDiv) {
            return new ChannelState(enabled, newPerDiv, zeroOffsetPixels, signal, lastSignal);
        }

        public ChannelState withOffset(int newOffset) {
            return new ChannelState(enabled, perDiv, newOffset, signal, lastSignal);
        }

        public ChannelState withSignalSource(Optional<BusSignalRef> newSignal) {
            return new ChannelState(enabled, perDiv, zeroOffsetPixels, newSignal, lastSignal);
        }

        public ChannelState withLastInput(int strength) {
            return new ChannelState(enabled, perDiv, zeroOffsetPixels, signal, strength);
        }
    }

    public enum TriggerChannel {
        LEFT, RIGHT, NONE;
        public static final MyCodec<TriggerChannel> CODEC = MyCodecs.forEnum(values(), TriggerChannel::ordinal);
    }
}
