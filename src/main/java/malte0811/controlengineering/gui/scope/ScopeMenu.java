package malte0811.controlengineering.gui.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.gui.CEContainerMenu;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.scope.FullSync;
import malte0811.controlengineering.network.scope.ScopePacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket.IScopeSubPacket;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.GlobalState;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.LambdaMutable;
import net.minecraft.world.inventory.MenuType;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ScopeMenu extends CEContainerMenu<IScopeSubPacket> {
    private final List<ModuleInScope> modules;
    private final Mutable<Traces> traces;
    private final Mutable<GlobalConfig> globalConfig;
    private final Mutable<GlobalState> globalState;

    public ScopeMenu(@Nullable MenuType<?> type, int id, ScopeBlockEntity scope) {
        super(type, id, isValidFor(scope), scope::setChanged, scope.getOpenMenus());
        this.modules = scope.getModules();
        this.traces = new LambdaMutable<>(scope::getTraces, scope::setTraces);
        this.globalConfig = new LambdaMutable<>(scope::getGlobalConfig, scope::setGlobalConfig);
        this.globalState = LambdaMutable.getterOnly(scope::getGlobalSyncState);
    }

    public ScopeMenu(MenuType<?> type, int id) {
        super(type, id);
        this.modules = new ArrayList<>();
        this.traces = new MutableObject<>(new Traces());
        this.globalConfig = new MutableObject<>(new GlobalConfig());
        this.globalState = new MutableObject<>(new GlobalState());
    }

    public List<ModuleInScope> getModules() {
        return modules;
    }

    public Traces getTraces() {
        return getTracesMutable().getValue();
    }

    public Mutable<Traces> getTracesMutable() {
        return traces;
    }

    @Override
    protected SimplePacket makePacket(IScopeSubPacket packet) {
        return new ScopePacket(packet);
    }

    @Override
    public IScopeSubPacket getInitialSync() {
        return new FullSync(getModules(), getTraces(), getGlobalConfig(), getGlobalState());
    }

    public GlobalConfig getGlobalConfig() {
        return getGlobalConfigMutable().getValue();
    }

    public Mutable<GlobalConfig> getGlobalConfigMutable() {
        return globalConfig;
    }

    public GlobalState getGlobalState() {
        return getGlobalStateMutable().getValue();
    }

    public Mutable<GlobalState> getGlobalStateMutable() {
        return globalState;
    }
}
