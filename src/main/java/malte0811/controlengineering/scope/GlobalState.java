package malte0811.controlengineering.scope;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;

public record GlobalState(boolean hasPower, int consumption) {
    public static final MyCodec<GlobalState> CODEC = new RecordCodec2<>(
            MyCodecs.BOOL.fieldOf("hasPower", GlobalState::hasPower),
            MyCodecs.INTEGER.fieldOf("consumption", GlobalState::consumption),
            GlobalState::new
    );

    public GlobalState() {
        this(false, 0);
    }
}
