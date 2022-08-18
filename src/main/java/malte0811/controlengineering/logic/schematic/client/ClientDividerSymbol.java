package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.cells.impl.VoltageDivider;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import net.minecraft.client.Minecraft;

public class ClientDividerSymbol extends ClientCellSymbol<Integer> {
    public ClientDividerSymbol(CellSymbol<Integer> serverSymbol) {
        super(serverSymbol, 24, 0);
    }

    @Override
    public void renderCustom(PoseStack transform, int x, int y, Integer rLower, int alpha) {
        super.renderCustom(transform, x, y, rLower, alpha);
        final var blackColor = alpha << 24;
        var font = Minecraft.getInstance().font;
        transform.pushPose();
        final var scale = 5f;
        transform.translate(5.25 + x, 2 + y + scale / 7.5, 0);
        transform.scale(1 / scale, 1 / scale, 1);
        font.draw(transform, Integer.toString(VoltageDivider.TOTAL_RESISTANCE - rLower), 0, 0, blackColor);
        transform.translate(0, 4 * scale, 0);
        font.draw(transform, Integer.toString(rLower), 0, 0, blackColor);
        transform.popPose();
    }
}
