package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.logic.cells.CellCost;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class IOSymbol extends SchematicSymbol<BusSignalRef> {
    public static final String ANALOG_INPUT_KEY = ControlEngineering.MODID + ".symbol.inputPinAnalog";
    public static final String DIGITAL_INPUT_KEY = ControlEngineering.MODID + ".symbol.inputPinDigital";
    public static final String OUTPUT_KEY = ControlEngineering.MODID + ".symbol.outputPin";
    public static final String SIGNAL_KEY = ControlEngineering.MODID + ".gui.busRef";
    public static final String VANILLA_COLOR_PREFIX = "item.minecraft.firework_star.";

    private static final List<SymbolPin> OUTPUT_PIN_A = ImmutableList.of(SymbolPin.analogOut(5, 1, "out"));
    private static final List<SymbolPin> OUTPUT_PIN_D = ImmutableList.of(SymbolPin.digitalOut(5, 1, "out"));
    private static final List<SymbolPin> INPUT_PIN = ImmutableList.of(SymbolPin.analogIn(0, 1, "in"));
    private final boolean isInput;
    private final boolean isDigitized;

    public IOSymbol(boolean isInput, boolean isDigitized) {
        super(BusSignalRef.DEFAULT, BusSignalRef.CODEC);
        Preconditions.checkArgument(isInput || !isDigitized);
        this.isInput = isInput;
        this.isDigitized = isDigitized;
    }

    @Override
    public int getXSize(BusSignalRef state, @Nonnull Level level) {
        return 6;
    }

    @Override
    public int getYSize(BusSignalRef state, @Nonnull Level level) {
        return 3;
    }

    @Override
    public List<SymbolPin> getPins(@Nullable BusSignalRef state) {
        if (!isInput) {
            return INPUT_PIN;
        } else if (isDigitized) {
            return OUTPUT_PIN_D;
        } else {
            return OUTPUT_PIN_A;
        }
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable(!isInput ? OUTPUT_KEY : (isDigitized ? DIGITAL_INPUT_KEY : ANALOG_INPUT_KEY));
    }

    @Override
    public List<MutableComponent> getExtraDescription(BusSignalRef state) {
        final DyeColor color = DyeColor.byId(state.color());
        final String colorName = I18n.get(VANILLA_COLOR_PREFIX + color);
        return ImmutableList.of(Component.translatable(SIGNAL_KEY, colorName, state.line()));
    }

    @Override
    public CellCost getCost() {
        if (isDigitized) {
            // Pay for builtin digitizer
            return SchematicSymbols.DIGITIZER.getCost();
        } else {
            return super.getCost();
        }
    }

    public boolean isInput() {
        return isInput;
    }
}
