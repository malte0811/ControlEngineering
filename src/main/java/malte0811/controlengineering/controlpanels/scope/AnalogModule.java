package malte0811.controlengineering.controlpanels.scope;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;

public class AnalogModule extends ScopeModule<AnalogModule.State> {
    public AnalogModule() {
        super(new State(true), State.CODEC, 1, false);
    }

    public record State(
            boolean risingTrigger
    ) {
        public static final MyCodec<State> CODEC = MyCodecs.BOOL.xmap(State::new, State::risingTrigger);
    }
}
