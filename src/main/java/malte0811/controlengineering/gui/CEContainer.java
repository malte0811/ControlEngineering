package malte0811.controlengineering.gui;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.network.SimplePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class CEContainer<PacketType> extends AbstractContainerMenu {
    private final List<ContainerListener> listeners = new ArrayList<>();
    protected final ContainerLevelAccess pos;
    @Nullable
    private final Block expectedBlock;

    protected CEContainer(@Nullable MenuType<?> type, ContainerLevelAccess pos, int id) {
        super(type, id);
        this.pos = pos;
        this.expectedBlock = this.pos.evaluate(Level::getBlockState).map(BlockState::getBlock).orElse(null);
    }

    @Override
    public boolean stillValid(@Nonnull Player playerIn) {
        return expectedBlock == null || stillValid(pos, playerIn, expectedBlock);
    }

    public void sendToListeningPlayersExcept(@Nullable ContainerListener excluded, PacketType data) {
        for (ContainerListener listener : listeners) {
            if (listener != excluded && listener instanceof ServerPlayer) {
                sendTo((ServerPlayer) listener, data);
            }
        }
    }

    @Override
    public void addSlotListener(@Nonnull ContainerListener listener) {
        super.addSlotListener(listener);
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            if (listener instanceof ServerPlayer) {
                sendTo((ServerPlayer) listener, getInitialSync());
            }
        }
    }

    @Override
    public void removeSlotListener(@Nonnull ContainerListener listener) {
        super.removeSlotListener(listener);
        listeners.remove(listener);
    }

    protected final void sendTo(ServerPlayer listener, PacketType packet) {
        ControlEngineering.NETWORK.sendTo(
                makePacket(packet), listener.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT
        );
    }

    protected abstract SimplePacket makePacket(PacketType type);

    protected abstract PacketType getInitialSync();

    public void markDirty() {
        pos.evaluate(Level::getBlockEntity).ifPresent(BlockEntity::setChanged);
    }
}
