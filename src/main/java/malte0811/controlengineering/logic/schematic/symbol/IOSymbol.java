package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusSignalRef;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.DyeColor;

import javax.annotation.Nullable;
import java.util.List;

public class IOSymbol extends SchematicSymbol<BusSignalRef> {
    public static final String INPUT_KEY = ControlEngineering.MODID + ".gui.inputPin";
    public static final String OUTPUT_KEY = ControlEngineering.MODID + ".gui.outputPin";
    public static final String SIGNAL_KEY = ControlEngineering.MODID + ".gui.busRef";
    public static final String VANILLA_COLOR_PREFIX = "item.minecraft.firework_star.";

    private static final List<SymbolPin> OUTPUT_PIN = ImmutableList.of(SymbolPin.analogOut(5, 1, "out"));
    private static final List<SymbolPin> INPUT_PIN = ImmutableList.of(SymbolPin.analogIn(0, 1, "in"));
    private final boolean isInput;

    public IOSymbol(boolean isInput) {
        super(BusSignalRef.DEFAULT, BusSignalRef.CODEC);
        this.isInput = isInput;
    }

    @Override
    public int getXSize() {
        return 6;
    }

    @Override
    public int getYSize() {
        return 3;
    }

    @Override
    public List<SymbolPin> getPins(@Nullable BusSignalRef state) {
        if (isInput) {
            return OUTPUT_PIN;
        } else {
            return INPUT_PIN;
        }
    }

    @Override
    public Component getName() {
        return new TranslatableComponent(isInput ? INPUT_KEY : OUTPUT_KEY);
    }

    @Override
    public List<MutableComponent> getExtraDescription(BusSignalRef state) {
        final DyeColor color = DyeColor.byId(state.color());
        final String colorName = I18n.get(VANILLA_COLOR_PREFIX + color);
        return ImmutableList.of(new TranslatableComponent(SIGNAL_KEY, colorName, state.line()));
    }

    public boolean isInput() {
        return isInput;
    }
}
