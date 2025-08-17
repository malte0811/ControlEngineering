package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.cells.impl.VoltageDivider;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ClientDividerSymbol extends ClientCellSymbol<Integer> {
    public ClientDividerSymbol(CellSymbol<Integer> serverSymbol) {
        super(serverSymbol, 24, 0);
    }

    @Override
    public void renderCustom(GuiGraphics graphics, int x, int y, Integer rLower, int alpha) {
        super.renderCustom(graphics, x, y, rLower, alpha);
        final var blackColor = alpha << 24;
        var font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        final var scale = 5f;
        graphics.pose().translate(5.25 + x, 2 + y + scale / 7.5, 0);
        graphics.pose().scale(1 / scale, 1 / scale, 1);
        graphics.drawString(font, Integer.toString(VoltageDivider.TOTAL_RESISTANCE - rLower), 0, 0, blackColor, false);
        graphics.pose().translate(0, 4 * scale, 0);
        graphics.drawString(font, Integer.toString(rLower), 0, 0, blackColor, false);
        graphics.pose().popPose();
    }
}
