package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CellSymbol extends SchematicSymbol<Unit> {
    private final LeafcellType<?> type;
    private final int width;
    private final int height;
    private final List<SymbolPin> pins;

    public CellSymbol(LeafcellType<?> type, int width, int height, List<SymbolPin> pins) {
        super(Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE));
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
    public int getXSize(Unit state, @Nonnull Level level) {
        return width;
    }

    @Override
    public int getYSize(Unit state, @Nonnull Level level) {
        return height;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public List<SymbolPin> getPins(@Nullable Unit unit) {
        return pins;
    }

    public static String getTranslationKey(LeafcellType<?> type) {
        return "cell." + type.getRegistryName().getNamespace() + "." + type.getRegistryName().getPath() + ".name";
    }

    @Override
    public Component getName() {
        return new TranslatableComponent(getTranslationKey(type));
    }

    public LeafcellType<?> getCellType() {
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
