package malte0811.controlengineering.gui.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.controlpanels.scope.ScopeModuleInstance;
import malte0811.controlengineering.gui.CEContainerMenu;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.scope.ScopePacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket.IScopeSubPacket;
import malte0811.controlengineering.network.scope.SyncModules;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ScopeMenu extends CEContainerMenu<IScopeSubPacket> {
    // TODO handle another player replacing a module
    private final List<ScopeModuleInstance<?>> modules;

    public ScopeMenu(@Nullable MenuType<?> type, int id, ScopeBlockEntity scope) {
        super(type, id, isValidFor(scope), scope::setChanged);
        this.modules = scope.getModules();
    }

    public ScopeMenu(MenuType<?> type, int id) {
        super(type, id);
        this.modules = new ArrayList<>();
    }

    public List<ScopeModuleInstance<?>> getModules() {
        return modules;
    }

    @Override
    protected SimplePacket makePacket(IScopeSubPacket packet) {
        return new ScopePacket(packet);
    }

    @Override
    protected IScopeSubPacket getInitialSync() {
        return new SyncModules(getModules());
    }
}
