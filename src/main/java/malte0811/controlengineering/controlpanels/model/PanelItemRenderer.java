package malte0811.controlengineering.controlpanels.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.controlpanels.PanelData;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class PanelItemRenderer extends ItemStackTileEntityRenderer {
    private static final PanelModelCache CACHE = new PanelModelCache();

    private final Function<ItemStack, PanelData> getData;

    public PanelItemRenderer(Function<ItemStack, PanelData> getData) {
        this.getData = getData;
    }

    @Override
    public void func_239207_a_(
            @Nonnull ItemStack stack,
            @Nonnull ItemCameraTransforms.TransformType transform,
            @Nonnull MatrixStack matrixStack, @Nonnull IRenderTypeBuffer buffer,
            int combinedLight, int combinedOverlay
    ) {
        PanelData data = getData.apply(stack);
        CACHE.getMixedModel(data).renderTo(buffer, matrixStack, combinedLight, combinedOverlay);
        TransformingVertexBuilder baseRender = new TransformingVertexBuilder(
                buffer.getBuffer(RenderType.getSolid()), matrixStack
        );
        baseRender.setLight(combinedLight);
        baseRender.setOverlay(combinedOverlay);
        PanelModelCache.renderPanel(data.getTransform(), baseRender);
    }
}
