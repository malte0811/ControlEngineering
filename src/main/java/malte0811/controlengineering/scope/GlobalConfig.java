package malte0811.controlengineering.scope;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;

public record GlobalConfig(int ticksPerDiv, boolean triggerArmed, boolean powered) {
    public static final MyCodec<GlobalConfig> CODEC = new RecordCodec3<>(
            MyCodecs.INTEGER.fieldOf("ticksPerDiv", GlobalConfig::ticksPerDiv),
            MyCodecs.BOOL.fieldOf("triggerArmed", GlobalConfig::triggerArmed),
            MyCodecs.BOOL.fieldOf("powered", GlobalConfig::powered),
            GlobalConfig::new
    );

    public GlobalConfig() {
        this(16, false, false);
    }

    public GlobalConfig withTicksPerDiv(int newTicksPerDiv) {
        return new GlobalConfig(newTicksPerDiv, triggerArmed, powered);
    }

    public GlobalConfig withTriggerArmed(boolean armed) {
        return new GlobalConfig(ticksPerDiv, armed, powered);
    }

    public GlobalConfig withPowered(boolean b) {
        return new GlobalConfig(ticksPerDiv, triggerArmed, b);
    }
}
