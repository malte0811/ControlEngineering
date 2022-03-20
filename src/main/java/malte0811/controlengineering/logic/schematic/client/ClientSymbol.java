package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.schematic.SchematicNet;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.logic.schematic.symbol.SymbolPin;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class ClientSymbol<State, Symbol extends SchematicSymbol<State>> {
    protected final Symbol serverSymbol;

    protected ClientSymbol(Symbol serverSymbol) {
        this.serverSymbol = serverSymbol;
    }

    protected abstract void renderCustom(PoseStack transform, int x, int y, @Nullable State state);

    public abstract void createInstanceWithUI(Consumer<? super SymbolInstance<State>> onDone, State initialState);

    public final void render(PoseStack stack, int x, int y, @Nullable State state) {
        renderCustom(stack, x, y, state);
        for (SymbolPin pin : serverSymbol.getPins(state)) {
            pin.render(stack, x, y, SchematicNet.WIRE_COLOR);
        }
    }
}
