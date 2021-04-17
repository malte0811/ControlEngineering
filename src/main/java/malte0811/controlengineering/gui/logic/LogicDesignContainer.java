package malte0811.controlengineering.gui.logic;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.gui.CustomDataContainerProvider;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.network.logic.FullSync;
import malte0811.controlengineering.network.logic.LogicPacket;
import malte0811.controlengineering.network.logic.LogicSubPacket;
import malte0811.controlengineering.tiles.logic.ISchematicTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LogicDesignContainer extends Container {
    private final List<IContainerListener> listeners = new ArrayList<>();
    private final IWorldPosCallable pos;
    @Nullable
    private final Block expectedBlock;
    public final boolean readOnly;

    public LogicDesignContainer(int id, IWorldPosCallable pos, boolean readOnly) {
        super(CEContainers.LOGIC_DESIGN.get(), id);
        this.pos = pos;
        this.readOnly = readOnly;
        this.expectedBlock = this.pos.apply(World::getBlockState).map(BlockState::getBlock).orElse(null);
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

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity playerIn) {
        return expectedBlock == null || isWithinUsableDistance(pos, playerIn, expectedBlock);
    }

    public Schematic getSchematic() {
        return pos.apply(World::getTileEntity)
                .map(te -> te instanceof ISchematicTile ? (ISchematicTile) te : null)
                .map(ISchematicTile::getSchematic)
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
