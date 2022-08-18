package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ClientCellSymbol<C> extends ClientSymbol<C, CellSymbol<C>> {
    private final SubTexture texture;

    public ClientCellSymbol(CellSymbol<C> serverSymbol, int uMin, int vMin) {
        super(serverSymbol);
        this.texture = makeSubtexture(serverSymbol, uMin, vMin);
    }

    @Override
    public void renderCustom(PoseStack transform, int x, int y, @Nullable C state, int alpha) {
        texture.blit(transform, x, y, alpha);
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<C>> onDone, C initialState) {
        DataProviderScreen<C> screen = DataProviderScreen.makeFor(
                Component.empty(),
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
