package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.gui.widget.IntSelector;
import malte0811.controlengineering.logic.cells.CircuitSignals;
import malte0811.controlengineering.logic.schematic.symbol.ConstantSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.util.RedstoneTapeUtils;
import malte0811.controlengineering.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static malte0811.controlengineering.logic.schematic.symbol.ConstantSymbol.BOX_SIZE;
import static malte0811.controlengineering.logic.schematic.symbol.ConstantSymbol.INPUT_KEY;

public class ClientConstantSymbol extends ClientSymbol<Integer, ConstantSymbol> {
    public ClientConstantSymbol(ConstantSymbol serverSymbol) {
        super(serverSymbol);
    }

    @Override
    public void renderCustom(PoseStack transform, int x, int y, @Nullable Integer state) {
        int color = RedstoneTapeUtils.getRSColor(state == null ? 0 : Math.abs(state / (float) BusLine.MAX_VALID_VALUE));
        GuiComponent.fill(
                transform, x + BOX_SIZE, y + BOX_SIZE / 2, x + BOX_SIZE + 1, y + BOX_SIZE / 2 + 1, color
        );
        final String text;
        if (state != null) {
            text = Integer.toString(state);
        } else {
            text = "";
        }
        TextUtil.renderBoxWithText(transform, color, text, 5, x, y, BOX_SIZE, BOX_SIZE);
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<Integer>> onDone, Integer initialState) {
        Minecraft.getInstance().setScreen(new IntSelector(
                i -> onDone.accept(new SymbolInstance<>(serverSymbol, i)),
                INPUT_KEY,
                CircuitSignals.MIN_VALID, CircuitSignals.MAX_VALID, initialState
        ));
    }
}
