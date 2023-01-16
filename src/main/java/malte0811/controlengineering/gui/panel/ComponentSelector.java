package malte0811.controlengineering.gui.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.gui.widget.NestedWidget;
import malte0811.controlengineering.gui.widget.PageSelector;
import malte0811.controlengineering.util.math.TransformUtil;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ComponentSelector extends NestedWidget {
    private static final int ROW_MIN_HEIGHT = 40;
    private static final Quaternionf MINUS_QUARTER_X = new Quaternionf().rotateX(-Mth.HALF_PI);

    private final Minecraft mc;
    private final List<PanelComponentType<?, ?>> available;
    private final int numCols = 2;
    private final int numRows;
    private final int actualRowHeight;
    private final int colWidth;
    private final Consumer<PanelComponentType<?, ?>> select;
    private final PageSelector pageSelector;

    public ComponentSelector(int x, int y, int width, int height, Consumer<PanelComponentType<?, ?>> select) {
        super(x, y, width, height);
        this.mc = Minecraft.getInstance();
        this.select = select;
        this.available = new ArrayList<>(PanelComponents.REGISTRY.getValues());
        final int displayHeight = height - PageSelector.HEIGHT;
        this.numRows = Math.max(displayHeight / ROW_MIN_HEIGHT, 1);
        this.actualRowHeight = displayHeight / numRows;
        this.colWidth = width / numCols;
        addWidget(this.pageSelector = new PageSelector(
                x, y + displayHeight, width, Mth.ceil(available.size() / (double) (numRows * numCols)), 0
        ));
    }

    @Override
    public void renderButton(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        final int selectedRow = (mouseY - getY()) / actualRowHeight;
        final int selectedCol = (mouseX - getX()) / colWidth;
        for (int col = 0; col < numCols; ++col) {
            for (int row = 0; row < numRows; ++row) {
                renderAvailableType(
                        matrixStack, getTypeIn(row, col),
                        getX() + col * colWidth, getY() + row * actualRowHeight,
                        col == selectedCol && row == selectedRow
                );
            }
        }
    }

    private void renderAvailableType(
            @Nonnull PoseStack transform, @Nullable PanelComponentType<?, ?> type, int x, int y, boolean highlight
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
            drawCenteredShrunkString(transform, mc.font, name, 0, colWidth, 0, 0);

            transform.translate(0, mc.font.lineHeight, 0);
            renderComponentInGui(transform, type, colWidth, actualRowHeight - mc.font.lineHeight * 1.5);
        }

        transform.popPose();
    }

    public static void renderComponentInGui(
            @Nonnull PoseStack transform, @Nonnull PanelComponentType<?, ?> type, double width, double height
    ) {
        transform.pushPose();
        var level = Minecraft.getInstance().level;
        var component = type.newInstance();
        var componentSize = component.getSize(level);
        var areaSize = new Vec2d(width, height);
        var extraScale = (float) Math.min(
                Math.min(areaSize.x() / componentSize.x(), areaSize.y() / componentSize.y()), 16
        );
        transform.translate(width / 2, height / 2, 0);
        transform.scale(extraScale, extraScale, .01f);
        transform.translate(-component.getSize(level).x() / 2f, -component.getSize(level).y() / 2f, 0);
        transform.mulPose(MINUS_QUARTER_X);
        TransformUtil.shear(transform, .1f, .1f);
        transform.scale(1, -1, 1);
        //TODO cache?
        var model = new MixedModel();
        ComponentRenderers.render(model, component, transform);
        var buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        model.renderTo(buffers, new PoseStack(), LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY);
        buffers.endBatch();
        transform.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < getX() || mouseY < getY() || mouseX > getX() + width || mouseY > getY() + height) {
            return false;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        final int row = (int) ((mouseY - getY()) / actualRowHeight);
        final int col = (int) ((mouseX - getX()) / colWidth);
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
        final int index = pageSelector.getCurrentPage() * numRows * numCols + row * numCols + col;
        if (index < 0 || index >= available.size()) {
            return null;
        } else {
            return available.get(index);
        }
    }

    @Override
    public void updateWidgetNarration(@Nonnull NarrationElementOutput pNarrationElementOutput) { }

    private static void drawCenteredShrunkString(
            PoseStack transform, Font font, String text, int xMin, int xMax, int y, int color
    ) {
        var textWidth = font.width(text);
        var areaWidth = xMax - xMin;
        var scale = Math.min(1, areaWidth / (float) textWidth);
        var areaCenter = xMin + areaWidth / 2.;
        transform.pushPose();
        transform.translate(areaCenter - scale * textWidth / 2., y, 0);
        transform.scale(scale, scale, 1);
        font.draw(transform, text, 0, 0, color);
        transform.popPose();
    }
}
