package malte0811.controlengineering.gui.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.gui.CEContainerMenu;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.scope.FullSync;
import malte0811.controlengineering.network.scope.ScopePacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket.IScopeSubPacket;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.LambdaMutable;
import net.minecraft.world.inventory.MenuType;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScopeMenu extends CEContainerMenu<IScopeSubPacket> {
    // TODO handle another player replacing a module
    private final List<ModuleInScope> modules;
    private final Mutable<Traces> traces;
    // TODO deduplicate with keypunch?
    private final Set<ScopeMenu> openMenusOnBE;
    private final Mutable<GlobalConfig> globalConfig;

    public ScopeMenu(@Nullable MenuType<?> type, int id, ScopeBlockEntity scope) {
        super(type, id, isValidFor(scope), scope::setChanged);
        this.modules = scope.getModules();
        this.openMenusOnBE = scope.getOpenMenus();
        this.traces = new LambdaMutable<>(
                scope::getTraces, $ -> { throw new RuntimeException("Cannot set traces on server"); }
        );
        this.globalConfig = new LambdaMutable<>(scope::getGlobalConfig, scope::setGlobalConfig);
    }

    public ScopeMenu(MenuType<?> type, int id) {
        super(type, id);
        this.modules = new ArrayList<>();
        this.openMenusOnBE = new HashSet<>();
        this.traces = new MutableObject<>(new Traces());
        this.globalConfig = new MutableObject<>(new GlobalConfig());
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
    protected IScopeSubPacket getInitialSync() {
        return new FullSync(getModules(), getTraces(), getGlobalConfig());
    }

    @Override
    protected void onFirstOpened() {
        super.onFirstOpened();
        openMenusOnBE.add(this);
    }

    @Override
    protected void onLastClosed() {
        super.onLastClosed();
        openMenusOnBE.remove(this);
    }

    public GlobalConfig getGlobalConfig() {
        return getGlobalConfigMutable().getValue();
    }

    public Mutable<GlobalConfig> getGlobalConfigMutable() {
        return globalConfig;
    }
}
