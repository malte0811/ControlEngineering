package malte0811.controlengineering.scope.module;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;
import malte0811.controlengineering.util.mycodec.record.RecordCodec4;

import java.util.ArrayList;
import java.util.List;

public class DigitalModule extends ScopeModule<DigitalModule.State> {
    public static final int NO_LINE = -1;
    private static final double SIGNAL_HEIGHT_DIVS = 0.5;
    private static final double TRACE_SEP_DIVS = 1;

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

    @Override
    public Pair<Boolean, State> isTriggered(State oldState, BusState input) {
        boolean triggered = true;
        for (int i = 0; i < BusLine.LINE_SIZE; ++i) {
            switch (oldState.inputState.channelTriggers().get(i)) {
                case LOW -> triggered &= !getSignal(input, oldState, i);
                case IGNORED -> { }
                case HIGH -> triggered &= getSignal(input, oldState, i);
            }
        }
        return Pair.of(triggered, oldState);
    }

    @Override
    public IntList getActiveTraces(State state) {
        final var result = new IntArrayList();
        for (int i = 0; i < BusLine.LINE_SIZE; ++i) {
            if (state.inputState.isChannelVisible(i)) {
                result.add(i);
            }
        }
        return result;
    }

    @Override
    public int getNumTraces() {
        return 16;
    }

    @Override
    public double getTraceValueInDivs(int traceId, BusState input, State currentState) {
        final var baseOffset = currentState.verticalOffset / (double) VERTICAL_DIV_PIXELS + TRACE_SEP_DIVS * traceId;
        return baseOffset + (getSignal(input, currentState, traceId) ? SIGNAL_HEIGHT_DIVS : 0);
    }

    private boolean getSignal(BusState input, State state, int traceId) {
        if (state.inputState.inputLine == NO_LINE) {
            return false;
        } else {
            return input.getLine(state.inputState.inputLine).getValue(traceId) > 0;
        }
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
