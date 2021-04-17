package malte0811.controlengineering.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class CustomDataContainerProvider implements INamedContainerProvider {
    private final ITextComponent name;
    private final IContainerProvider inner;
    private final Consumer<PacketBuffer> writeExtra;

    public CustomDataContainerProvider(
            ITextComponent name, IContainerProvider inner, Consumer<PacketBuffer> writeExtra
    ) {
        this.name = name;
        this.inner = inner;
        this.writeExtra = writeExtra;
    }

    public Consumer<PacketBuffer> extraData() {
        return writeExtra;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return this.name;
    }

    @Override
    public Container createMenu(int id, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return this.inner.createMenu(id, inv, player);
    }

    public void open(ServerPlayerEntity player) {
        NetworkHooks.openGui(player, this, extraData());
    }
}
