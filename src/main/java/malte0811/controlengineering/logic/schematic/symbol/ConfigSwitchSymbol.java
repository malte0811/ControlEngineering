package malte0811.controlengineering.logic.schematic.symbol;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.impl.ConfigSwitch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;

import java.util.List;

import static malte0811.controlengineering.logic.schematic.symbol.SymbolPin.digitalOut;

public class ConfigSwitchSymbol extends CellSymbol<Boolean> {
    public static final String ON_KEY = ControlEngineering.MODID + ".cell.config_switch.on";
    public static final String OFF_KEY = ControlEngineering.MODID + ".cell.config_switch.off";

    public ConfigSwitchSymbol() {
        super(Leafcells.CONFIG_SWITCH, 7, 8, List.of(
                digitalOut(6, 4, ConfigSwitch.DEFAULT_OUT_NAME)
        ));
    }

    @Override
    public Component getName(Boolean state) {
        return getDefaultName().copy().append(": ").append(Component.translatable(state ? ON_KEY : OFF_KEY));
    }

    @Override
    public boolean canConfigureOnReadOnly() {
        return true;
    }
}
