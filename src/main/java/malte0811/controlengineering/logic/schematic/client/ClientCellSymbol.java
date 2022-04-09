package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static malte0811.controlengineering.logic.schematic.symbol.SchematicSymbols.SYMBOLS_SHEET;

public class ClientCellSymbol<C> extends ClientSymbol<C, CellSymbol<C>> {
    private final SubTexture texture;

    public ClientCellSymbol(CellSymbol<C> serverSymbol, int uMin, int vMin) {
        super(serverSymbol);
        this.texture = new SubTexture(
                SYMBOLS_SHEET, uMin, vMin, uMin + serverSymbol.getWidth(), vMin + serverSymbol.getHeight(), 64
        );
    }

    @Override
    public void renderCustom(PoseStack transform, int x, int y, @Nullable C state) {
        texture.blit(transform, x, y);
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<C>> onDone, C initialState) {
        DataProviderScreen<C> screen = DataProviderScreen.makeFor(
                TextComponent.EMPTY,
                initialState, serverSymbol.getStateCodec(),
                config -> onDone.accept(serverSymbol.newInstance(config))

        );
        if (screen != null) {
            Minecraft.getInstance().setScreen(screen);
        } else {
            onDone.accept(serverSymbol.newInstance());
        }
    }
}
