package malte0811.controlengineering.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

public class PageSelector extends NestedWidget {
    public static final int HEIGHT = 20;
    private final int numPages;
    private int currentPage;

    public PageSelector(int x, int y, int width, int numPages, int currentPage) {
        super(x, y, width, HEIGHT);
        this.numPages = numPages;
        this.currentPage = Mth.clamp(currentPage, 0, numPages - 1);
        if (numPages > 1) {
            addWidget(new Button(
                    x, y, width / 3, HEIGHT,
                    Component.literal("<-"), $ -> this.currentPage = Math.max(0, this.currentPage - 1)
            ));
            addWidget(new Button(
                    x + 2 * width / 3, y, width / 3, HEIGHT,
                    Component.literal("->"), $ -> this.currentPage = Math.min(numPages - 1, this.currentPage + 1)
            ));
        }
    }

    @Override
    public void render(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTick) {
        super.render(transform, mouseX, mouseY, partialTick);
        if (numPages > 1) {
            var font = Minecraft.getInstance().font;
            drawCenteredString(
                    transform, font, (currentPage + 1) + " / " + numPages,
                    x + width / 2, y + (HEIGHT - font.lineHeight) / 2, -1
            );
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
