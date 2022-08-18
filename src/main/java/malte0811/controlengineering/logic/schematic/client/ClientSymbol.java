package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.logic.schematic.SchematicNet;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.logic.schematic.symbol.SymbolPin;
import malte0811.controlengineering.util.ColorUtils;

import java.util.function.Consumer;

import static malte0811.controlengineering.logic.schematic.symbol.SchematicSymbols.SYMBOLS_SHEET;

public abstract class ClientSymbol<State, Symbol extends SchematicSymbol<State>> {
    protected final Symbol serverSymbol;

    protected ClientSymbol(Symbol serverSymbol) {
        this.serverSymbol = serverSymbol;
    }

    protected abstract void renderCustom(PoseStack transform, int x, int y, State state, int alpha);

    public abstract void createInstanceWithUI(Consumer<? super SymbolInstance<State>> onDone, State initialState);

    public final void render(PoseStack stack, int x, int y, State state, int alpha) {
        renderCustom(stack, x, y, state, alpha);
        for (SymbolPin pin : serverSymbol.getPins(state)) {
            pin.render(stack, x, y, SchematicNet.WIRE_COLOR, alpha);
        }
    }

    protected static SubTexture makeSubtexture(CellSymbol<?> serverSymbol, int uMin, int vMin) {
        return new SubTexture(
                SYMBOLS_SHEET, uMin, vMin, uMin + serverSymbol.getWidth(), vMin + serverSymbol.getHeight(), 64
        );
    }
}
