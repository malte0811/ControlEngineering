package malte0811.controlengineering.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.render.tape.TapeDrive;
import malte0811.controlengineering.tiles.panels.PanelCNCTile;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Quaternion;

import javax.annotation.Nonnull;

public class PanelCNCRenderer extends TileEntityRenderer<PanelCNCTile> {

    public PanelCNCRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(
            @Nonnull PanelCNCTile tileEntityIn,
            float partialTicks,
            @Nonnull MatrixStack matrixStackIn,
            @Nonnull IRenderTypeBuffer bufferIn,
            int combinedLightIn,
            int combinedOverlayIn
    ) {
        //TODO put into TE in some way?
        final long totLength = 300;
        TapeDrive testWheel = new TapeDrive(
                totLength, 2, 1,
                new Vec2d(5, 8), new Vec2d(7, 5),
                new Vec2d(11, 8), new Vec2d(9, 5)
        );
        long now = Util.milliTime();
        testWheel.updateTapeProgress((now / 200.) % totLength);
        matrixStackIn.push();
        rotateAroundCenter(-PanelCNCBlock.getDirection(tileEntityIn).getHorizontalAngle(), matrixStackIn);
        matrixStackIn.scale(1 / 16f, 1 / 16f, 1 / 16f);
        matrixStackIn.translate(0, 14, 0);
        testWheel.render(bufferIn.getBuffer(RenderType.getSolid()), matrixStackIn, combinedLightIn, combinedOverlayIn);
        matrixStackIn.pop();
    }

    private void rotateAroundCenter(double angleDegress, MatrixStack stack) {
        stack.translate(.5, .5, .5);
        stack.rotate(new Quaternion(0, (float) angleDegress, 0, true));
        stack.translate(-.5, -.5, -.5);
    }
}
