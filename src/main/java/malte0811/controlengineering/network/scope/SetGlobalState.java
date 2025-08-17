package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.GlobalState;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.mycodec.MyCodec;
import org.apache.commons.lang3.mutable.Mutable;

import java.util.List;

public record SetGlobalState(GlobalState newState) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<SetGlobalState> CODEC = GlobalState.CODEC.xmap(
            SetGlobalState::new, SetGlobalState::newState
    );

    @Override
    public boolean process(
            List<ModuleInScope> modules,
            Mutable<Traces> traces,
            Mutable<GlobalConfig> globalConfig,
            Mutable<GlobalState> globalState
    ) {
        globalState.setValue(newState);
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
