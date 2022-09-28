package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.mycodec.MyCodec;
import org.apache.commons.lang3.mutable.Mutable;

import javax.annotation.Nullable;
import java.util.List;

public record SetGlobalCfg(GlobalConfig newCfg) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<SetGlobalCfg> CODEC = GlobalConfig.CODEC.xmap(
            SetGlobalCfg::new, SetGlobalCfg::newCfg
    );

    @Override
    public boolean process(
            List<ModuleInScope> modules, @Nullable Mutable<Traces> traces, Mutable<GlobalConfig> globalConfig
    ) {
        globalConfig.setValue(newCfg);
        return true;
    }
}
