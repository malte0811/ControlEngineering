package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.GlobalState;
import malte0811.controlengineering.scope.trace.Trace;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec4;
import org.apache.commons.lang3.mutable.Mutable;

import javax.annotation.Nullable;
import java.util.List;

public record FullSync(
        List<ModuleInScope> newModules, Traces traces, GlobalConfig globalCfg, GlobalState globalState
) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<FullSync> CODEC = new RecordCodec4<>(
            MyCodecs.list(ModuleInScope.CODEC).fieldOf("modules", FullSync::newModules),
            Traces.CODEC.fieldOf("traces", FullSync::traces),
            GlobalConfig.CODEC.fieldOf("globalCfg", FullSync::globalCfg),
            GlobalState.CODEC.fieldOf("globalState", FullSync::globalState),
            FullSync::new
    );

    @Override
    public boolean process(
            List<ModuleInScope> modules,
            @Nullable Mutable<Traces> traces,
            Mutable<GlobalConfig> globalConfig,
            Mutable<GlobalState> globalState
    ) {
        if (traces == null) {
            return false;
        }
        modules.clear();
        modules.addAll(newModules);
        List<Trace> copiedTraces = this.traces.traces().stream().map(Trace::new).toList();
        traces.setValue(new Traces(copiedTraces, this.traces.ticksPerDiv()));
        globalConfig.setValue(globalCfg);
        globalState.setValue(this.globalState);
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
