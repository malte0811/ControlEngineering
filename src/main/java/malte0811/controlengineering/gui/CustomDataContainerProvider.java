package malte0811.controlengineering.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class CustomDataContainerProvider implements MenuProvider {
    private final Component name;
    private final MenuConstructor inner;
    private final Consumer<FriendlyByteBuf> writeExtra;

    public CustomDataContainerProvider(
            Component name, MenuConstructor inner, Consumer<FriendlyByteBuf> writeExtra
    ) {
        this.name = name;
        this.inner = inner;
        this.writeExtra = writeExtra;
    }

    public Consumer<FriendlyByteBuf> extraData() {
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
        NetworkHooks.openGui(player, this, extraData());
    }
}
