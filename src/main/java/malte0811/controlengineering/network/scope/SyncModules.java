package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.scope.trace.Trace;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import javax.annotation.Nullable;
import java.util.List;

// TODO include full trace sync in this?
public record SyncModules(List<ModuleInScope> newModules) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<SyncModules> CODEC = MyCodecs.list(ModuleInScope.CODEC)
            .xmap(SyncModules::new, SyncModules::newModules);

    @Override
    public boolean process(List<ModuleInScope> modules, @Nullable List<Trace> traces) {
        modules.clear();
        modules.addAll(newModules);
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
