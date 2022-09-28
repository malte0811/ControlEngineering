package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.network.scope.ScopeSubPacket.IScopeSubPacket;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import org.apache.commons.lang3.mutable.Mutable;

import java.util.List;

public class ResetSweep implements IScopeSubPacket {
    public static final MyCodec<ResetSweep> CODEC = MyCodecs.unit(new ResetSweep());

    @Override
    public boolean process(
            List<ModuleInScope> modules, Mutable<Traces> traces, Mutable<GlobalConfig> globalConfig
    ) {
        globalConfig.setValue(globalConfig.getValue().withTriggerArmed(false));
        traces.setValue(new Traces());
        return true;
    }
}
