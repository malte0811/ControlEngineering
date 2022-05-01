package malte0811.controlengineering.logic.schematic.symbol;

import malte0811.controlengineering.logic.cells.LeafcellInstance;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

import java.util.List;

public class SymbolInstance<State> extends TypedInstance<State, SchematicSymbol<State>> {
    public static final MyCodec<SymbolInstance<?>> CODEC = makeCodec(SchematicSymbols.REGISTRY);

    public SymbolInstance(SchematicSymbol<State> stateSchematicSymbol, State currentState) {
        super(stateSchematicSymbol, currentState);
    }

    public int getXSize(Level level) {
        return getType().getXSize(getCurrentState(), level);
    }

    public int getYSize(Level level) {
        return getType().getYSize(getCurrentState(), level);
    }

    public Component getName() {
        return getType().getName(currentState);
    }

    public List<MutableComponent> getExtraDesc() {
        return getType().getExtraDescription(getCurrentState());
    }

    public List<SymbolPin> getPins() {
        return getType().getPins(getCurrentState());
    }

    @Override
    public String toString() {
        return "[type=" + getType() + ", state=" + getCurrentState() + "]";
    }

    public LeafcellInstance<?, State> makeCell() {
        if (!(getType() instanceof CellSymbol<State> cell)) {
            throw new RuntimeException("Expected cell symbol, got " + getType().getRegistryName());
        }
        return cell.getCellType().newInstanceFromConfig(getCurrentState());
    }
}
