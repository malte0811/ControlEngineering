package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.util.math.Fraction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import javax.annotation.Nullable;

public class ClientInvAmpSymbol extends ClientCellSymbol<Fraction> {
    public ClientInvAmpSymbol(CellSymbol<Fraction> symbol) {
        super(symbol, 24, 18);
    }

    @Override
    public void renderCustom(PoseStack transform, int x, int y, @Nullable Fraction state, int alpha) {
        super.renderCustom(transform, x, y, state, alpha);
        if (state != null) {
            var font = Minecraft.getInstance().font;
            final int blackColor = alpha << 24;
            transform.pushPose();
            final var scale = 4f;
            transform.translate(x + 1, y + 1, 0);
            transform.scale(1 / scale, 1 / scale, 1);

            transform.pushPose();
            transform.translate(scale * serverSymbol.getWidth() / 2f, 0, 0);
            renderFractionNumber(transform, font, state.numerator(), 1);
            renderFractionNumber(transform, font, state.denominator(), 11);
            // Fraction line
            transform.translate(-scale * 2.4, 6, 0);
            transform.scale(3.6f, 1, 1);
            font.draw(transform, "-", 0, 0, blackColor);
            transform.popPose();

            // Minus
            transform.translate(2 * scale + 1, 6, 0);
            font.draw(transform, "-", 0, 0, blackColor);
            transform.popPose();
        }
    }

    private void renderFractionNumber(PoseStack transform, Font font, int value, int yPos) {
        final var desc = Integer.toString(value);
        font.draw(transform, desc, -font.width(desc) / 2f, yPos, 0);
    }
}
