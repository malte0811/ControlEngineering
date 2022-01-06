package malte0811.controlengineering.gui.logic;

import malte0811.controlengineering.blockentity.logic.ISchematicBE;
import malte0811.controlengineering.blockentity.logic.LogicWorkbenchBlockEntity;
import malte0811.controlengineering.blockentity.logic.LogicWorkbenchBlockEntity.AvailableIngredients;
import malte0811.controlengineering.gui.CEContainerMenu;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.logic.FullSync;
import malte0811.controlengineering.network.logic.LogicPacket;
import malte0811.controlengineering.network.logic.LogicSubPacket;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;

public class LogicDesignMenu extends CEContainerMenu<LogicSubPacket> {
    public final boolean readOnly;
    private final Schematic schematic;
    @Nullable
    private final AvailableIngredients availableIngredients;

    public <BE extends BlockEntity & ISchematicBE>
    LogicDesignMenu(MenuType<?> type, int id, BE schematicBE, boolean readOnly) {
        super(type, id, ContainerScreenManager.isValidFor(schematicBE), schematicBE::setChanged);
        this.readOnly = readOnly;
        this.schematic = schematicBE.getSchematic();
        if (!readOnly) {
            var logicWorkbench = (LogicWorkbenchBlockEntity) schematicBE;
            availableIngredients = logicWorkbench.getCosts();
        } else {
            availableIngredients = null;
        }
        addSlots();
    }

    public LogicDesignMenu(MenuType<?> type, int id, boolean readOnly) {
        super(type, id);
        this.schematic = new Schematic();
        this.readOnly = readOnly;
        availableIngredients = readOnly ? null : new AvailableIngredients();
        addSlots();
    }

    private void addSlots() {
        if (availableIngredients != null) {
            addSlot(availableIngredients.makeTubeSlot(0));
            addSlot(availableIngredients.makeWireSlot(1));
        }
    }

    public static LogicDesignMenuType makeType(String name, boolean readOnly, DeferredRegister<MenuType<?>> register) {
        var type = register.register(name, () -> {
            Mutable<MenuType<LogicDesignMenu>> typeBox = new MutableObject<>();
            typeBox.setValue(new MenuType<>((id, inv) -> new LogicDesignMenu(typeBox.getValue(), id, readOnly)));
            return typeBox.getValue();
        });
        return new LogicDesignMenuType(type, readOnly);
    }

    @Nullable
    public AvailableIngredients getAvailableIngredients() {
        return availableIngredients;
    }

    public Schematic getSchematic() {
        return schematic;
    }

    @Override
    protected SimplePacket makePacket(LogicSubPacket subPacket) {
        return new LogicPacket(subPacket);
    }

    @Override
    protected LogicSubPacket getInitialSync() {
        return new FullSync(getSchematic());
    }

    public record LogicDesignMenuType(RegistryObject<MenuType<LogicDesignMenu>> type, boolean readOnly) {
        public <T extends BlockEntity & ISchematicBE> LogicDesignMenu makeNew(int id, T blockEntity) {
            return new LogicDesignMenu(type.get(), id, blockEntity, readOnly);
        }

        public MenuType<? extends LogicDesignMenu> get() {
            return type.get();
        }
    }
}
