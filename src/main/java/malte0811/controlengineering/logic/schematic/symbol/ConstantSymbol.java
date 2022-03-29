package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ConstantSymbol extends SchematicSymbol<Double> {
    public static final String INPUT_KEY = ControlEngineering.MODID + ".gui.analogStrength";
    public static final String NAME = ControlEngineering.MODID + ".symbol.constantSymbol";
    public static final int BOX_SIZE = 5;

    private static final List<SymbolPin> DIGITAL = ImmutableList.of(SymbolPin.digitalOut(
            BOX_SIZE + 2,
            BOX_SIZE / 2,
            "out"
    ));
    private static final List<SymbolPin> ANALOG = ImmutableList.of(SymbolPin.analogOut(
            BOX_SIZE + 2,
            BOX_SIZE / 2,
            "out"
    ));

    public ConstantSymbol() {
        super(0., MyCodecs.DOUBLE);
    }

    @Override
    public int getXSize(Double state, @Nonnull Level level) {
        return 6;
    }

    @Override
    public int getYSize(Double state, @Nonnull Level level) {
        return 3;
    }

    @Override
    public List<SymbolPin> getPins(@Nullable Double state) {
        if (state == null || (state != 0 && state != 1)) {
            return ANALOG;
        } else {
            return DIGITAL;
        }
    }

    @Override
    public Component getName() {
        return new TranslatableComponent(NAME);
    }

    @Override
    public List<MutableComponent> getExtraDescription(Double state) {
        return ImmutableList.of(new TranslatableComponent(INPUT_KEY, (int) (state * BusLine.MAX_VALID_VALUE)));
    }
}
