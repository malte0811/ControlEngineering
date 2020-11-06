package malte0811.controlengineering.gui;

import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.tiles.tape.TeletypeTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Optional;

public class TeletypeContainer extends Container {
    private final IWorldPosCallable pos;

    public TeletypeContainer(int id, PacketBuffer data) {
        this(id, ContainerScreenManager.readWorldPos(data));
    }

    public TeletypeContainer(int id, IWorldPosCallable pos) {
        super(CEContainers.TELETYPE.get(), id);
        this.pos = pos;
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity playerIn) {
        return isWithinUsableDistance(pos, playerIn, CEBlocks.TELETYPE.get());
    }

    public Optional<TeletypeTile> getTeletype() {
        return pos.apply(World::getTileEntity)
                .map(te -> te instanceof TeletypeTile ? (TeletypeTile) te : null);
    }

    public void typeAll(byte[] typed) {
        getTeletype().ifPresent(tile -> tile.type(typed));
    }
}
