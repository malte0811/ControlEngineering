package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.GlobalState;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.mycodec.MyCodec;
import org.apache.commons.lang3.mutable.Mutable;

import java.util.List;

public record SetGlobalCfg(GlobalConfig newCfg) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<SetGlobalCfg> CODEC = GlobalConfig.CODEC.xmap(
            SetGlobalCfg::new, SetGlobalCfg::newCfg
    );

    @Override
    public boolean process(
            List<ModuleInScope> modules,
            Mutable<Traces> traces,
            Mutable<GlobalConfig> globalConfig,
            Mutable<GlobalState> globalState
    ) {
        if (!globalState.getValue().hasPower() && newCfg.powered()) { return false; }
        globalConfig.setValue(newCfg);
        if (!newCfg.powered() && !traces.getValue().traces().isEmpty()) {
            traces.setValue(new Traces());
        }
        return true;
    }
}
