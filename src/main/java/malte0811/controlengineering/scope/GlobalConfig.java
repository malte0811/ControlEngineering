package malte0811.controlengineering.scope;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;

public record GlobalConfig(int ticksPerDiv, boolean triggerArmed) {
    public static final MyCodec<GlobalConfig> CODEC = new RecordCodec2<>(
            MyCodecs.INTEGER.fieldOf("ticksPerDiv", GlobalConfig::ticksPerDiv),
            MyCodecs.BOOL.fieldOf("triggerArmed", GlobalConfig::triggerArmed),
            GlobalConfig::new
    );

    public GlobalConfig() {
        this(16, false);
    }

    public GlobalConfig withTicksPerDiv(int newTicksPerDiv) {
        return new GlobalConfig(newTicksPerDiv, triggerArmed);
    }

    public GlobalConfig withTriggerArmed(boolean armed) {
        return new GlobalConfig(ticksPerDiv, armed);
    }
}
