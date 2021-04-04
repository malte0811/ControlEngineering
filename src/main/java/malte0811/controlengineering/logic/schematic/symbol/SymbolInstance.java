package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.util.typereg.TypedInstance;
import net.minecraft.util.text.ITextComponent;

public class SymbolInstance<State> extends TypedInstance<State, SchematicSymbol<State>> {
    public SymbolInstance(SchematicSymbol<State> stateSchematicSymbol, State currentState) {
        super(stateSchematicSymbol, currentState);
    }

    public void render(MatrixStack transform, int x, int y) {
        getType().render(transform, x, y, getCurrentState());
    }

    public int getXSize() {
        return getType().getXSize();
    }

    public int getYSize() {
        return getType().getYSize();
    }

    public ITextComponent getDesc() {
        return getType().getDesc();
    }
}
