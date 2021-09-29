package malte0811.controlengineering.controlpanels.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.controlpanels.PanelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class PanelItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final PanelModelCache CACHE = new PanelModelCache();

    private final Function<ItemStack, PanelData> getData;

    public PanelItemRenderer(Function<ItemStack, PanelData> getData) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.getData = getData;
    }

    @Override
    public void renderByItem(
            @Nonnull ItemStack stack,
            @Nonnull ItemTransforms.TransformType transform,
            @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer,
            int combinedLight, int combinedOverlay
    ) {
        PanelData data = getData.apply(stack);
        CACHE.getMixedModel(data).renderTo(buffer, matrixStack, combinedLight, combinedOverlay);
        TransformingVertexBuilder baseRender = new TransformingVertexBuilder(
                buffer.getBuffer(RenderType.solid()), matrixStack, DefaultVertexFormat.BLOCK
        );
        baseRender.setLight(combinedLight);
        baseRender.setOverlay(combinedOverlay);
        PanelModelCache.renderPanel(data.getTransform(), baseRender);
    }
}
