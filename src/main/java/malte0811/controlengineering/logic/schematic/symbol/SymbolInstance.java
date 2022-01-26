package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class SymbolInstance<State> extends TypedInstance<State, SchematicSymbol<State>> {
    public static final MyCodec<SymbolInstance<?>> CODEC = makeCodec(SchematicSymbols.REGISTRY);

    public SymbolInstance(SchematicSymbol<State> stateSchematicSymbol, State currentState) {
        super(stateSchematicSymbol, currentState);
    }

    public void render(PoseStack transform, int x, int y) {
        getType().render(transform, x, y, getCurrentState());
    }

    public int getXSize() {
        return getType().getXSize();
    }

    public int getYSize() {
        return getType().getYSize();
    }

    public Component getName() {
        return getType().getName();
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
}
