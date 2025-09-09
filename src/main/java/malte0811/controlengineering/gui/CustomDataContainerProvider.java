package malte0811.controlengineering.gui;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

// TODO unused?
public record CustomDataContainerProvider(
        Component name, MenuConstructor inner, Consumer<RegistryFriendlyByteBuf> writeExtra
) implements MenuProvider {

    public Consumer<RegistryFriendlyByteBuf> extraData() {
        return writeExtra;
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return this.name;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inv, @Nonnull Player player) {
        return this.inner.createMenu(id, inv, player);
    }

    public void open(ServerPlayer player) {
        player.openMenu(this, extraData());
    }
}
