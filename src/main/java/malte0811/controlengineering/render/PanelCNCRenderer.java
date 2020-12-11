package malte0811.controlengineering.render;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.render.tape.TapeDrive;
import malte0811.controlengineering.tiles.panels.PanelCNCTile;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Quaternion;

import javax.annotation.Nonnull;

public class PanelCNCRenderer extends TileEntityRenderer<PanelCNCTile> {

    public PanelCNCRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(
            @Nonnull PanelCNCTile tile,
            float partialTicks,
            @Nonnull MatrixStack transform,
            @Nonnull IRenderTypeBuffer buffers,
            int light,
            int overlay
    ) {
        transform.push();
        rotateAroundCenter(-PanelCNCBlock.getDirection(tile).getHorizontalAngle(), transform);
        transform.scale(1 / 16f, 1 / 16f, 1 / 16f);
        final double tick = tile.getCurrentTicksInJob() + partialTicks;
        transform.push();
        transform.translate(0, 14, 0);
        renderTape(tile, buffers, transform, light, overlay, tick);
        transform.pop();
        renderCurrentPanelState(tile, buffers, transform, light, overlay, tick);
        transform.pop();
    }

    private void rotateAroundCenter(double angleDegrees, MatrixStack stack) {
        stack.translate(.5, .5, .5);
        stack.rotate(new Quaternion(0, (float) angleDegrees, 0, true));
        stack.translate(-.5, -.5, -.5);
    }

    private void renderCurrentPanelState(
            PanelCNCTile tile, IRenderTypeBuffer buffers, MatrixStack transform, int light, int overlay, double ticks
    ) {
        PanelCNCTile.CNCJob job = tile.getCurrentJob();
        if (job != null) {
            transform.push();
            transform.translate(1, 2, 1);
            transform.scale(14f / 16, 14f / 16, 14f / 16);
            IVertexBuilder builder = buffers.getBuffer(RenderType.getSolid());
            for (PlacedComponent placed : job.getComponentsAtTime(ticks)) {
                PanelRenderer.renderComponent(transform, placed, builder, light, overlay);
            }
            transform.pop();
        }
    }

    private void renderTape(
            PanelCNCTile tile, IRenderTypeBuffer buffer, MatrixStack transform, int light, int overlay, double ticks
    ) {
        final long totLength = tile.getTapeLength();
        if (totLength > 0) {
            PanelCNCTile.CNCJob currentJob = tile.getCurrentJob();
            Preconditions.checkNotNull(currentJob);
            //TODO put into TE in some way? Or make static(ish)?
            TapeDrive testWheel = new TapeDrive(
                    totLength + 1, 2, 1,
                    new Vec2d(5, 8), new Vec2d(7, 5),
                    new Vec2d(11, 8), new Vec2d(9, 5)
            );
            testWheel.updateTapeProgress(currentJob.getTapeProgressAtTime(ticks));
            testWheel.render(buffer.getBuffer(RenderType.getSolid()), transform, light, overlay);
        }
    }
}
