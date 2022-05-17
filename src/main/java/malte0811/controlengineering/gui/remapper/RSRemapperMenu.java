package malte0811.controlengineering.gui.remapper;

import malte0811.controlengineering.blockentity.bus.RSRemapperBlockEntity;
import malte0811.controlengineering.bus.BusLine;
import net.minecraft.world.inventory.MenuType;

public class RSRemapperMenu extends AbstractRemapperMenu {
    public RSRemapperMenu(MenuType<?> type, int id, RSRemapperBlockEntity bEntity) {
        super(type, id, bEntity, bEntity::setColorToGray, bEntity::getColorToGray);
    }

    public RSRemapperMenu(MenuType<?> type, int id) {
        super(type, id, BusLine.LINE_SIZE);
    }
}
