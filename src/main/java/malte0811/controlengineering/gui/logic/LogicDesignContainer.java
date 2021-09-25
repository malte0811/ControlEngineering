package malte0811.controlengineering.gui.logic;

import malte0811.controlengineering.gui.CEContainer;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.gui.CustomDataContainerProvider;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.logic.FullSync;
import malte0811.controlengineering.network.logic.LogicPacket;
import malte0811.controlengineering.network.logic.LogicSubPacket;
import malte0811.controlengineering.tiles.logic.ISchematicTile;
import malte0811.controlengineering.tiles.logic.LogicWorkbenchTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import java.util.Optional;

public class LogicDesignContainer extends CEContainer<LogicSubPacket> {
    public final boolean readOnly;

    public LogicDesignContainer(int id, ContainerLevelAccess pos, boolean readOnly) {
        super(CEContainers.LOGIC_DESIGN.get(), pos, id);
        this.readOnly = readOnly;
    }

    public LogicDesignContainer(int id, FriendlyByteBuf data) {
        this(id, ContainerScreenManager.readWorldPos(data), data.readBoolean());
    }

    public static CustomDataContainerProvider makeProvider(Level worldIn, BlockPos pos, boolean readOnly) {
        return new CustomDataContainerProvider(
                new TranslatableComponent("screen.controlengineering.logic_design"),
                (id, inv, player) -> new LogicDesignContainer(id, ContainerLevelAccess.create(worldIn, pos), readOnly),
                buffer -> {
                    buffer.writeBlockPos(pos);
                    buffer.writeBoolean(readOnly);
                }
        );
    }

    public Optional<LogicWorkbenchTile.AvailableIngredients> getAvailableIngredients() {
        return getTile(LogicWorkbenchTile.class).map(LogicWorkbenchTile::getCosts);
    }

    public Schematic getSchematic() {
        return getTile(ISchematicTile.class)
                .map(ISchematicTile::getSchematic)
                .orElseThrow(RuntimeException::new);
    }

    private <T> Optional<T> getTile(Class<T> tileType) {
        return pos.evaluate(Level::getBlockEntity)
                .map(te -> tileType.isAssignableFrom(te.getClass()) ? tileType.cast(te) : null);
    }

    @Override
    protected SimplePacket makePacket(LogicSubPacket subPacket) {
        return new LogicPacket(subPacket);
    }

    @Override
    protected LogicSubPacket getInitialSync() {
        return new FullSync(getSchematic());
    }
}
