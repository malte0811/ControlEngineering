package malte0811.controlengineering.scope;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;

public record GlobalConfig(int ticksPerDiv) {
    public static final MyCodec<GlobalConfig> CODEC = MyCodecs.INTEGER.xmap(
            GlobalConfig::new, GlobalConfig::ticksPerDiv
    );

    public GlobalConfig() {
        this(16);
    }

    public GlobalConfig withTicksPerDiv(int newTicksPerDiv) {
        return new GlobalConfig(newTicksPerDiv);
    }
}
