package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.controlpanels.scope.ScopeModuleInstance;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import java.util.List;

public record SyncModules(List<ScopeModuleInstance<?>> newModules) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<SyncModules> CODEC = MyCodecs.list(ScopeModuleInstance.CODEC)
            .xmap(SyncModules::new, SyncModules::newModules);

    @Override
    public boolean process(List<ScopeModuleInstance<?>> modules) {
        modules.clear();
        modules.addAll(newModules);
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
