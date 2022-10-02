package malte0811.controlengineering.gui.scope;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.gui.StackedScreen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class ZoomedCRTScreen extends StackedScreen {
    private static final int BORDER_SIZE = 20;

    private final ScopeMenu menu;
    private CRTDisplay crt;

    public ZoomedCRTScreen(ScopeMenu menu) {
        super(Component.empty());
        this.menu = menu;
    }

    @Override
    protected void init() {
        super.init();
        crt = new CRTDisplay(menu, BORDER_SIZE, BORDER_SIZE, width - 2 * BORDER_SIZE, height - 2 * BORDER_SIZE);
    }

    @Override
    protected void renderForeground(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTicks) {
        crt.draw(transform, null);
    }
}
