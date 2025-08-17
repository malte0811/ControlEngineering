package malte0811.controlengineering.logic.schematic.client;

import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public class ClientConfigSwitch extends ClientSymbol<Boolean, CellSymbol<Boolean>> {
    private final SubTexture onTexture;
    private final SubTexture offTexture;

    public ClientConfigSwitch(CellSymbol<Boolean> serverSymbol) {
        super(serverSymbol);
        this.onTexture = makeSubtexture(serverSymbol, 26, 33);
        this.offTexture = makeSubtexture(serverSymbol, 26, 25);
    }

    @Override
    protected void renderCustom(GuiGraphics graphics, int x, int y, Boolean state, int alpha) {
        (state ? this.onTexture : this.offTexture).blit(graphics.pose(), x, y, alpha);
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<Boolean>> onDone, Boolean initialState) {
        onDone.accept(new SymbolInstance<>(serverSymbol, !initialState));
    }
}
