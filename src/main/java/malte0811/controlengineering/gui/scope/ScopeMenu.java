package malte0811.controlengineering.gui.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.gui.CEContainerMenu;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.scope.FullSync;
import malte0811.controlengineering.network.scope.ScopePacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket.IScopeSubPacket;
import malte0811.controlengineering.scope.trace.Trace;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nullable;
import java.util.*;

public class ScopeMenu extends CEContainerMenu<IScopeSubPacket> {
    // TODO handle another player replacing a module
    private final List<ModuleInScope> modules;
    private final List<Trace> traces;
    // TODO deduplicate with keypunch?
    private final Set<ScopeMenu> openMenusOnBE;

    public ScopeMenu(@Nullable MenuType<?> type, int id, ScopeBlockEntity scope) {
        super(type, id, isValidFor(scope), scope::setChanged);
        this.modules = scope.getModules();
        this.openMenusOnBE = scope.getOpenMenus();
        this.traces = Collections.unmodifiableList(scope.getTraces());
    }

    public ScopeMenu(MenuType<?> type, int id) {
        super(type, id);
        this.modules = new ArrayList<>();
        this.openMenusOnBE = new HashSet<>();
        this.traces = new ArrayList<>();
    }

    public List<ModuleInScope> getModules() {
        return modules;
    }

    public List<Trace> getTraces() {
        return traces;
    }

    @Override
    protected SimplePacket makePacket(IScopeSubPacket packet) {
        return new ScopePacket(packet);
    }

    @Override
    protected IScopeSubPacket getInitialSync() {
        return new FullSync(getModules(), getTraces());
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
}
