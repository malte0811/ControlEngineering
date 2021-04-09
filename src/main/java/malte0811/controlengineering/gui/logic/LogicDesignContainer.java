package malte0811.controlengineering.gui.logic;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.network.logic.FullSync;
import malte0811.controlengineering.network.logic.LogicPacket;
import malte0811.controlengineering.network.logic.LogicSubPacket;
import malte0811.controlengineering.tiles.logic.LogicWorkbenchTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LogicDesignContainer extends Container {
    private final List<IContainerListener> listeners = new ArrayList<>();
    private final IWorldPosCallable pos;

    public LogicDesignContainer(int id, IWorldPosCallable pos) {
        super(CEContainers.LOGIC_DESIGN.get(), id);
        this.pos = pos;
    }

    public LogicDesignContainer(int id, PacketBuffer data) {
        super(CEContainers.LOGIC_DESIGN.get(), id);
        this.pos = ContainerScreenManager.readWorldPos(data);
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity playerIn) {
        return isWithinUsableDistance(pos, playerIn, CEBlocks.LOGIC_WORKBENCH.get());
    }

    public Schematic getSchematic() {
        return pos.apply(World::getTileEntity)
                .map(te -> te instanceof LogicWorkbenchTile ? (LogicWorkbenchTile) te : null)
                .map(LogicWorkbenchTile::getSchematic)
                .orElseThrow(RuntimeException::new);
    }

    public void sendToListeningPlayersExcept(@Nullable ServerPlayerEntity excluded, LogicSubPacket data) {
        for (IContainerListener listener : listeners) {
            if (listener != excluded) {
                sendTo(listener, data);
            }
        }
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            sendTo(listener, new FullSync(getSchematic()));
        }
    }

    @Override
    public void removeListener(@Nonnull IContainerListener listener) {
        super.removeListener(listener);
        listeners.remove(listener);
    }

    private void sendTo(IContainerListener listener, LogicSubPacket packet) {
        if (listener instanceof ServerPlayerEntity) {
            ControlEngineering.NETWORK.sendTo(
                    new LogicPacket(packet),
                    ((ServerPlayerEntity) listener).connection.getNetworkManager(),
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }
    }
}
