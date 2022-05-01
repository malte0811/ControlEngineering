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

public class ConstantSymbol extends SchematicSymbol<Integer> {
    public static final String INPUT_KEY = ControlEngineering.MODID + ".gui.analogStrength";
    public static final String NAME = ControlEngineering.MODID + ".symbol.constantSymbol";
    public static final int BOX_SIZE = 5;

    private static final List<SymbolPin> DIGITAL = ImmutableList.of(
            SymbolPin.digitalOut(BOX_SIZE + 2, BOX_SIZE / 2, "out")
    );
    private static final List<SymbolPin> ANALOG = ImmutableList.of(
            SymbolPin.analogOut(BOX_SIZE + 2, BOX_SIZE / 2, "out")
    );

    public ConstantSymbol() {
        super(0, MyCodecs.INTEGER);
    }

    @Override
    public int getXSize(Integer state, @Nonnull Level level) {
        return 6;
    }

    @Override
    public int getYSize(Integer state, @Nonnull Level level) {
        return 5;
    }

    @Override
    public List<SymbolPin> getPins(@Nullable Integer state) {
        if (state == null || (state != BusLine.MIN_VALID_VALUE && state != BusLine.MAX_VALID_VALUE)) {
            return ANALOG;
        } else {
            return DIGITAL;
        }
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent(NAME);
    }

    @Override
    public List<MutableComponent> getExtraDescription(Integer state) {
        return ImmutableList.of(new TranslatableComponent(INPUT_KEY, state));
    }
}
