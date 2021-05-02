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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class LogicDesignContainer extends CEContainer<LogicSubPacket> {
    public final boolean readOnly;

    public LogicDesignContainer(int id, IWorldPosCallable pos, boolean readOnly) {
        super(CEContainers.LOGIC_DESIGN.get(), pos, id);
        this.readOnly = readOnly;
    }

    public LogicDesignContainer(int id, PacketBuffer data) {
        this(id, ContainerScreenManager.readWorldPos(data), data.readBoolean());
    }

    public static CustomDataContainerProvider makeProvider(World worldIn, BlockPos pos, boolean readOnly) {
        return new CustomDataContainerProvider(
                new TranslationTextComponent("screen.controlengineering.logic_design"),
                (id, inv, player) -> new LogicDesignContainer(id, IWorldPosCallable.of(worldIn, pos), readOnly),
                buffer -> {
                    buffer.writeBlockPos(pos);
                    buffer.writeBoolean(readOnly);
                }
        );
    }

    public Schematic getSchematic() {
        return pos.apply(World::getTileEntity)
                .map(te -> te instanceof ISchematicTile ? (ISchematicTile) te : null)
                .map(ISchematicTile::getSchematic)
                .orElseThrow(RuntimeException::new);
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
