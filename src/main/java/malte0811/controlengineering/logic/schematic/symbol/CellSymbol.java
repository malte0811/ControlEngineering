package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.cells.LeafcellType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CellSymbol<Config> extends SchematicSymbol<Config> {
    private final LeafcellType<?, Config> type;
    private final int width;
    private final int height;
    private final List<SymbolPin> pins;

    public CellSymbol(LeafcellType<?, Config> type, int width, int height, List<SymbolPin> pins) {
        super(type.getInitialState().getSecond(), type.getConfigCodec());
        this.type = type;
        this.width = width;
        this.height = height;
        this.pins = pins;
        for (SymbolPin pin : pins) {
            if (pin.isOutput()) {
                Preconditions.checkState(type.getOutputPins().containsKey(pin.pinName()));
            } else {
                Preconditions.checkState(type.getInputPins().containsKey(pin.pinName()));
            }
        }
    }

    @Override
    public int getXSize(Config state, @Nonnull Level level) {
        return width;
    }

    @Override
    public int getYSize(Config state, @Nonnull Level level) {
        return height;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public List<SymbolPin> getPins(@Nullable Config unit) {
        return pins;
    }

    public static String getTranslationKey(LeafcellType<?, ?> type) {
        return "cell." + type.getRegistryName().getNamespace() + "." + type.getRegistryName().getPath() + ".name";
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent(getTranslationKey(type));
    }

    public LeafcellType<?, Config> getCellType() {
        return type;
    }

    @Override
    public CellCost getCost() {
        return type.getCost();
    }

    @Override
    public String toString() {
        return "[Cell:" + getCellType() + "]";
    }
}
