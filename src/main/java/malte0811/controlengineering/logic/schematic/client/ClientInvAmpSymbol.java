package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.util.math.Fraction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;

public class ClientInvAmpSymbol extends ClientCellSymbol<Fraction> {
    public ClientInvAmpSymbol(CellSymbol<Fraction> symbol) {
        super(symbol, 24, 18);
    }

    @Override
    public void renderCustom(GuiGraphics graphics, int x, int y, @Nullable Fraction state, int alpha) {
        super.renderCustom(graphics, x, y, state, alpha);
        if (state != null) {
            var font = Minecraft.getInstance().font;
            final int blackColor = alpha << 24;
            graphics.pose().pushPose();
            final var scale = 4f;
            graphics.pose().translate(x + 1, y + 1, 0);
            graphics.pose().scale(1 / scale, 1 / scale, 1);

            graphics.pose().pushPose();
            graphics.pose().translate(scale * serverSymbol.getWidth() / 2f, 0, 0);
            renderFractionNumber(graphics, font, state.numerator(), 1);
            renderFractionNumber(graphics, font, state.denominator(), 11);
            // Fraction line
            graphics.pose().translate(-scale * 2.4, 6, 0);
            graphics.pose().scale(3.6f, 1, 1);
            graphics.drawString(font, "-", 0, 0, blackColor, false);
            graphics.pose().popPose();

            // Minus
            graphics.pose().translate(2 * scale + 1, 6, 0);
            graphics.drawString(font, "-", 0, 0, blackColor, false);
            graphics.pose().popPose();
        }
    }

    private void renderFractionNumber(GuiGraphics graphics, Font font, int value, int yPos) {
        final var desc = Integer.toString(value);
        graphics.drawString(font, desc, (int) (-font.width(desc) / 2f), yPos, 0, false);
    }
}
