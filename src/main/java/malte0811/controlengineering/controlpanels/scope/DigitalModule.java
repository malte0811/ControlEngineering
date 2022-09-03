package malte0811.controlengineering.controlpanels.scope;

import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import java.util.ArrayList;
import java.util.List;

public class DigitalModule extends ScopeModule<DigitalModule.State> {
    public DigitalModule() {
        super(new State(new ArrayList<>()), State.CODEC, 2, false);
    }

    public enum TriggerState {
        LOW, IGNORED, HIGH
    }

    public record State(List<TriggerState> channelTriggers) {
        public static final MyCodec<State> CODEC = MyCodecs.list(
                MyCodecs.INTEGER.xmap(i -> TriggerState.values()[i], TriggerState::ordinal)
        ).xmap(State::new, State::channelTriggers);

        public State {
            while (channelTriggers.size() < BusLine.LINE_SIZE) {
                channelTriggers.add(TriggerState.IGNORED);
            }
        }

        public State withTrigger(int channel, TriggerState newState) {
            final var newTriggers = new ArrayList<>(channelTriggers);
            newTriggers.set(channel, newState);
            return new State(newTriggers);
        }
    }
}
