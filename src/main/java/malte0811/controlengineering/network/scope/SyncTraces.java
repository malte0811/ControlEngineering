package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.scope.trace.Trace;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import javax.annotation.Nullable;
import java.util.List;

// TODO add diff-based version rather than always syncing the full thing
public record SyncTraces(List<Trace> traces) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<SyncTraces> CODEC = MyCodecs.list(Trace.CODEC)
            .xmap(SyncTraces::new, SyncTraces::traces);

    @Override
    public boolean process(List<ScopeBlockEntity.ModuleInScope> modules, @Nullable List<Trace> traces) {
        if (traces == null) {
            return false;
        }
        traces.clear();
        for (final var trace : this.traces) {
            traces.add(new Trace(trace));
        }
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
