package malte0811.controlengineering.gui.panel;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Quaternion;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.util.math.TransformUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ComponentSelector extends AbstractWidget {
    private static final int ROW_MIN_HEIGHT = 40;
    private static final int BUTTON_HEIGHT = 20;

    private final Minecraft mc;
    private final List<PanelComponentType<?, ?>> available;
    private final int numCols = 2;
    private final int numRows;
    private final int actualRowHeight;
    private final int colWidth;
    private final int numPages;
    private final List<AbstractWidget> buttons;
    private final Consumer<PanelComponentType<?, ?>> select;

    private int page = 0;

    public ComponentSelector(int x, int y, int width, int height, Consumer<PanelComponentType<?, ?>> select) {
        super(x, y, width, height, TextComponent.EMPTY);
        this.mc = Minecraft.getInstance();
        this.select = select;
        this.available = new ArrayList<>(PanelComponents.REGISTRY.getValues());
        final int displayHeight = height - BUTTON_HEIGHT;
        this.numRows = Math.max(displayHeight / ROW_MIN_HEIGHT, 1);
        this.actualRowHeight = displayHeight / numRows;
        this.colWidth = width / numCols;
        this.numPages = Mth.ceil(available.size() / (double) (numRows * numCols));
        this.buttons = ImmutableList.of(
                new Button(
                        x, y + displayHeight, width / 3, BUTTON_HEIGHT,
                        new TextComponent("<-"), $ -> page = Math.max(0, page - 1)
                ),
                new Button(
                        x + 2 * width / 3, y + displayHeight, width / 3, BUTTON_HEIGHT,
                        new TextComponent("->"), $ -> page = Math.min(numPages - 1, page + 1)
                )
        );
    }

    @Override
    public void renderButton(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (AbstractWidget w : buttons) {
            w.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        drawCenteredString(
                matrixStack, mc.font, (page + 1) + " / " + numPages,
                x + width / 2, y + height - (BUTTON_HEIGHT + mc.font.lineHeight) / 2, -1
        );
        final int selectedRow = (mouseY - y) / actualRowHeight;
        final int selectedCol = (mouseX - x) / colWidth;
        for (int col = 0; col < numCols; ++col) {
            for (int row = 0; row < numRows; ++row) {
                renderAvailableType(
                        matrixStack, getTypeIn(row, col),
                        x + col * colWidth, y + row * actualRowHeight,
                        col == selectedCol && row == selectedRow
                );
            }
        }
    }

    private <C, S> void renderAvailableType(
            @Nonnull PoseStack transform, @Nullable PanelComponentType<C, S> type, int x, int y, boolean highlight
    ) {
        transform.pushPose();
        transform.translate(x, y, 0);
        if (highlight && type != null) {
            fill(transform, 0, 0, colWidth, actualRowHeight, 0xffaaaaff);
        } else {
            fill(transform, 0, 0, colWidth, actualRowHeight, 0xffaaaaaa);
        }
        if (type != null) {
            transform.translate(0, 1, 0);
            String name = I18n.get(type.getTranslationKey());
            mc.font.draw(transform, name, (colWidth - mc.font.width(name)) / 2f, 0, 0);

            transform.translate(colWidth / 2., (actualRowHeight + mc.font.lineHeight) / 2., 0);
            transform.scale(16, 16, .01f);
            PanelComponentInstance<?, ?> instance = type.newInstance();
            transform.translate(-instance.getSize().x / 2f, -instance.getSize().y / 2f, 0);
            transform.mulPose(new Quaternion(-90, 0, 0, true));
            TransformUtil.shear(transform, .1f, .1f);
            transform.scale(1, -1, 1);
            //TODO cache?
            MixedModel model = new MixedModel();
            ComponentRenderers.render(model, instance, transform);
            MultiBufferSource.BufferSource impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            model.renderTo(impl, new PoseStack(), LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY);
            impl.endBatch();
        }

        transform.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseY < y || mouseX > x + width || mouseY > y + height) {
            return false;
        }
        for (AbstractWidget w : buttons) {
            if (w.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        final int row = (int) ((mouseY - y) / actualRowHeight);
        final int col = (int) ((mouseX - x) / colWidth);
        if (row >= 0 && row < numRows && col >= 0 && col < numCols) {
            PanelComponentType<?, ?> type = getTypeIn(row, col);
            if (type != null) {
                select.accept(type);
                return true;
            }
        }
        return false;
    }

    @Nullable
    private PanelComponentType<?, ?> getTypeIn(int row, int col) {
        final int index = page * numRows * numCols + row * numCols + col;
        if (index < 0 || index >= available.size()) {
            return null;
        } else {
            return available.get(index);
        }
    }
}
