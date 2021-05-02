package malte0811.controlengineering.gui;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.network.SimplePacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class CEContainer<PacketType> extends Container {
    private final List<IContainerListener> listeners = new ArrayList<>();
    protected final IWorldPosCallable pos;
    @Nullable
    private final Block expectedBlock;

    protected CEContainer(@Nullable ContainerType<?> type, IWorldPosCallable pos, int id) {
        super(type, id);
        this.pos = pos;
        this.expectedBlock = this.pos.apply(World::getBlockState).map(BlockState::getBlock).orElse(null);
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity playerIn) {
        return expectedBlock == null || isWithinUsableDistance(pos, playerIn, expectedBlock);
    }

    public void sendToListeningPlayersExcept(@Nullable IContainerListener excluded, PacketType data) {
        for (IContainerListener listener : listeners) {
            if (listener != excluded && listener instanceof ServerPlayerEntity) {
                sendTo((ServerPlayerEntity) listener, data);
            }
        }
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            if (listener instanceof ServerPlayerEntity) {
                sendTo((ServerPlayerEntity) listener, getInitialSync());
            }
        }
    }

    @Override
    public void removeListener(@Nonnull IContainerListener listener) {
        super.removeListener(listener);
        listeners.remove(listener);
    }

    protected final void sendTo(ServerPlayerEntity listener, PacketType packet) {
        ControlEngineering.NETWORK.sendTo(
                makePacket(packet), listener.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT
        );
    }

    protected abstract SimplePacket makePacket(PacketType type);

    protected abstract PacketType getInitialSync();

    public void markDirty() {
        pos.apply(World::getTileEntity).ifPresent(TileEntity::markDirty);
    }
}
