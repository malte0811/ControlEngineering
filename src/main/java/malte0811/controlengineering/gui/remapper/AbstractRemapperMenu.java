package malte0811.controlengineering.gui.remapper;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.CEContainerMenu;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.remapper.FullSync;
import malte0811.controlengineering.network.remapper.RemapperPacket;
import malte0811.controlengineering.network.remapper.RemapperSubPacket;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractRemapperMenu extends CEContainerMenu<RemapperSubPacket> {
    public static final int NOT_MAPPED = -1;

    private final Consumer<int[]> setMapping;
    private final Supplier<int[]> getMapping;

    protected AbstractRemapperMenu(
            MenuType<?> type, int id, BlockEntity bEntity, Consumer<int[]> setMapping, Supplier<int[]> getMapping
    ) {
        super(type, id, isValidFor(bEntity), bEntity::setChanged);
        this.setMapping = setMapping;
        this.getMapping = getMapping;
    }

    protected AbstractRemapperMenu(MenuType<?> type, int id, int mappingSize) {
        super(type, id);
        Mutable<int[]> mapping = new MutableObject<>(new int[mappingSize]);
        this.getMapping = mapping::getValue;
        this.setMapping = mapping::setValue;
    }

    @Override
    protected SimplePacket makePacket(RemapperSubPacket subPacket) {
        return new RemapperPacket(subPacket);
    }

    @Override
    protected RemapperSubPacket getInitialSync() {
        return new FullSync(getMapping());
    }

    public void setMapping(int[] newMapping) {
        this.setMapping.accept(newMapping);
    }

    public int[] getMapping() {
        var original = this.getMapping.get();
        return Arrays.copyOf(original, original.length);
    }

    public void processAndSend(RemapperSubPacket packet) {
        var fullPacket = new RemapperPacket(packet);
        fullPacket.updateConnections(this);
        ControlEngineering.NETWORK.sendToServer(fullPacket);
    }
}
