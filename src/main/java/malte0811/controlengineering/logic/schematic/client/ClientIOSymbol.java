package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.logic.schematic.symbol.IOSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.util.ColorUtils;
import malte0811.controlengineering.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ClientIOSymbol extends ClientSymbol<BusSignalRef, IOSymbol> {
    public ClientIOSymbol(IOSymbol serverSymbol) {
        super(serverSymbol);
    }

    @Override
    protected void renderCustom(PoseStack transform, int x, int y, @Nullable BusSignalRef state, int alpha) {
        int color;
        if (state != null) {
            color = DyeColor.byId(state.color()).getTextColor();
        } else {
            color = 0;
        }
        color = ColorUtils.withAlpha(color, alpha);
        if (serverSymbol.isInput()) {
            GuiComponent.fill(transform, x + 3, y + 1, x + 4, y + 2, color);
        } else {
            GuiComponent.fill(transform, x + 2, y + 1, x + 3, y + 2, color);
        }
        final String text = state != null ? Integer.toString(state.line()) : "";
        final int blockX = x + (serverSymbol.isInput() ? 0 : 3);
        TextUtil.renderBoxWithText(transform, color, text, 4, blockX, y, 3, 3);
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<BusSignalRef>> onDone, BusSignalRef initialState) {
        Minecraft.getInstance().setScreen(DataProviderScreen.makeFor(
                Component.empty(), initialState, BusSignalRef.CODEC,
                ref -> {
                    SymbolInstance<BusSignalRef> instance = new SymbolInstance<>(serverSymbol, ref);
                    onDone.accept(instance);
                }
        ));
    }
}
