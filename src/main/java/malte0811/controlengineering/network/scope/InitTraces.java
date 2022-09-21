package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.scope.trace.Trace;
import malte0811.controlengineering.scope.trace.TraceId;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import javax.annotation.Nullable;
import java.util.List;

public record InitTraces(List<TraceId> ids) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<InitTraces> CODEC = MyCodecs.list(TraceId.CODEC).xmap(InitTraces::new, InitTraces::ids);

    @Override
    public boolean process(List<ScopeBlockEntity.ModuleInScope> modules, @Nullable List<Trace> traces) {
        if (traces == null) {
            return false;
        }
        traces.clear();
        for (final var id : ids) {
            traces.add(new Trace(id));
        }
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
