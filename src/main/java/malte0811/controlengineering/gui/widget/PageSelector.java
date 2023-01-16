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
            addWidget(Button.builder(Component.literal("<-"), $ -> this.currentPage = Math.max(0, this.currentPage - 1))
                    .pos(x, y)
                    .size(width / 3, HEIGHT)
                    .build());
            addWidget(Button.builder(
                            Component.literal("->"), $ -> this.currentPage = Math.min(numPages - 1, this.currentPage + 1)
                    )
                    .pos(x + 2 * width / 3, y)
                    .size(width / 3, HEIGHT)
                    .build());
        }
    }

    @Override
    public void render(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTick) {
        super.render(transform, mouseX, mouseY, partialTick);
        if (numPages > 1) {
            var font = Minecraft.getInstance().font;
            drawCenteredString(
                    transform, font, (currentPage + 1) + " / " + numPages,
                    getX() + width / 2, getY() + (HEIGHT - font.lineHeight) / 2, -1
            );
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
