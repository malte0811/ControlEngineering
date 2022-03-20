package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static malte0811.controlengineering.logic.schematic.symbol.SchematicSymbols.SYMBOLS_SHEET;

public class ClientCellSymbol extends ClientSymbol<Unit, CellSymbol> {
    private final SubTexture texture;

    public ClientCellSymbol(CellSymbol serverSymbol, int uMin, int vMin) {
        super(serverSymbol);
        this.texture = new SubTexture(
                SYMBOLS_SHEET,
                uMin, vMin,
                uMin + serverSymbol.getXSize(), vMin + serverSymbol.getYSize(),
                64
        );
    }

    @Override
    public void renderCustom(PoseStack transform, int x, int y, @Nullable Unit state) {
        texture.blit(transform, x, y);
    }

    @Override
    public void createInstanceWithUI(
            Consumer<? super SymbolInstance<Unit>> onDone,
            Unit initialState
    ) {
        // No config required/possible
        onDone.accept(serverSymbol.newInstance());
    }
}
