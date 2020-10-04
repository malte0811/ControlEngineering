package malte0811.controlengineering.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.tiles.panels.PanelTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

import javax.annotation.Nonnull;

//TODO baked model? At least partially?
public class PanelRenderer extends TileEntityRenderer<PanelTileEntity> {
    public PanelRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(
            PanelTileEntity tile,
            float partialTicks,
            @Nonnull MatrixStack transform,
            IRenderTypeBuffer buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        BlockState state = tile.getBlockState();
        if (state.get(PanelBlock.IS_BASE)) {
            return;
        }
        tile = PanelBlock.getBase(tile.getWorld(), state, tile.getPos());
        if (tile == null) {
            return;
        }
        tile.getTransform().getPanelTopToWorld().toTransformationMatrix().push(transform);
        final float baseScale = 1 / 16f;
        transform.scale(baseScale, baseScale, baseScale);
        IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
        for (PlacedComponent comp : tile.getComponents()) {
            transform.push();
            transform.translate(comp.getPos().x, 0, comp.getPos().y);
            ComponentRenderers.render(builder, comp.getComponent(), transform, combinedLight, combinedOverlay);
            transform.pop();
        }
        transform.pop();
    }
}
