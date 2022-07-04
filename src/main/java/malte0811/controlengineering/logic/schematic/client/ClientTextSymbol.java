package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.logic.schematic.symbol.TextSymbol;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ClientTextSymbol extends ClientSymbol<String, TextSymbol> {
    public ClientTextSymbol(TextSymbol symbol) {
        super(symbol);
    }

    @Override
    protected void renderCustom(PoseStack transform, int x, int y, String s) {
        transform.pushPose();
        transform.translate(x, y, 0);
        transform.scale((float) TextSymbol.SCALE, (float) TextSymbol.SCALE, 1);
        Minecraft.getInstance().font.draw(transform, s, 0, 0, 0);
        transform.popPose();
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<String>> onDone, String initialState) {
        Minecraft.getInstance().setScreen(DataProviderScreen.makeFor(
                Component.empty(),
                initialState,
                MyCodecs.STRING,
                s -> onDone.accept(new SymbolInstance<>(serverSymbol, s))
        ));
    }
}
