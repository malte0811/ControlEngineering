package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.GlobalState;
import malte0811.controlengineering.scope.trace.Trace;
import malte0811.controlengineering.scope.trace.TraceId;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import org.apache.commons.lang3.mutable.Mutable;

import java.util.ArrayList;
import java.util.List;

public record InitTraces(List<TraceId> ids, int ticksPerDiv) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<InitTraces> CODEC = new RecordCodec2<>(
            MyCodecs.list(TraceId.CODEC).fieldOf("ids", InitTraces::ids),
            MyCodecs.INTEGER.fieldOf("ticksPerDiv", InitTraces::ticksPerDiv),
            InitTraces::new
    );

    public static InitTraces createForModules(List<ModuleInScope> modules, int ticksPerDiv) {
        final List<TraceId> traceIds = new ArrayList<>();
        for (final var module : modules) {
            if (!module.module().isEnabled()) { continue; }
            for (final var traceId : module.module().getActiveTraces()) {
                traceIds.add(new TraceId(module.firstSlot(), traceId));
            }
        }
        return new InitTraces(traceIds, ticksPerDiv);
    }

    @Override
    public boolean process(
            List<ModuleInScope> modules,
            Mutable<Traces> traces,
            Mutable<GlobalConfig> globalConfig,
            Mutable<GlobalState> globalState
    ) {
        if (traces == null) {
            return false;
        }
        List<Trace> traceList = new ArrayList<>(ids.size());
        for (final var id : ids) {
            traceList.add(new Trace(id));
        }
        traces.setValue(new Traces(traceList, ticksPerDiv));
        globalConfig.setValue(globalConfig.getValue().withTriggerArmed(false));
        return true;
    }
}
