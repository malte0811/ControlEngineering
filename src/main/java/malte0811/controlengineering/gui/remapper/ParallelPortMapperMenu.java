package malte0811.controlengineering.gui.remapper;

import malte0811.controlengineering.blockentity.bus.IParallelPortOwner;
import malte0811.controlengineering.blockentity.bus.ParallelPort;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.RegistryObject;

public class ParallelPortMapperMenu extends AbstractRemapperMenu {
    ParallelPortMapperMenu(MenuType<?> type, int id, BlockEntity bEntity, ParallelPort port) {
        super(type, id, bEntity, port::setIndicesFromRemapping, port::getIndicesForRemapping);
    }

    public ParallelPortMapperMenu(MenuType<?> type, int id) {
        super(type, id, Byte.SIZE + 1);
    }

    public record Type(RegistryObject<MenuType<ParallelPortMapperMenu>> type) {
        public <T extends BlockEntity & IParallelPortOwner>
        MenuProvider provider(BlockEntity blockEntity, ParallelPort port) {
            return new SimpleMenuProvider(
                    (id, inv, player) -> new ParallelPortMapperMenu(type.get(), id, blockEntity, port),
                    Component.empty()
            );
        }

        public MenuType<? extends ParallelPortMapperMenu> get() {
            return type.get();
        }
    }
}
