package malte0811.controlengineering.gui.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.StackedScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class ConfirmScreen extends StackedScreen {
    public static final String OK_KEY = ControlEngineering.MODID + ".gui.ok";
    public static final String CANCEL_KEY = ControlEngineering.MODID + ".gui.cancel";

    private final Runnable onOk;

    public ConfirmScreen(Component message, Runnable onOk) {
        super(message);
        this.onOk = onOk;
    }

    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable(OK_KEY), $ -> {
                    onOk.run();
                    onClose();
                })
                .pos(this.width / 2 - 155, 110)
                .size(150, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable(CANCEL_KEY), $ -> onClose())
                .pos(this.width / 2 - 155 + 160, 110)
                .size(150, 20)
                .build());
    }

    @Override
    protected void renderForeground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 90, -1);
    }
}
