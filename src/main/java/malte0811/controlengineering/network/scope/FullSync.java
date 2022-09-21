package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.scope.trace.Trace;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;

import javax.annotation.Nullable;
import java.util.List;

public record FullSync(List<ModuleInScope> newModules, List<Trace> traces) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<FullSync> CODEC = new RecordCodec2<>(
            MyCodecs.list(ModuleInScope.CODEC).fieldOf("modules", FullSync::newModules),
            MyCodecs.list(Trace.CODEC).fieldOf("traces", FullSync::traces),
            FullSync::new
    );

    @Override
    public boolean process(List<ModuleInScope> modules, @Nullable List<Trace> traces) {
        if (traces == null) {
            return false;
        }
        modules.clear();
        modules.addAll(newModules);
        traces.clear();
        // Copy traces, I don't trust the "memory connection" used in singleplayer
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
