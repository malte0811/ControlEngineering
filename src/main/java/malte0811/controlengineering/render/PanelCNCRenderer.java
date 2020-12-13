package malte0811.controlengineering.render;

import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.renders.RenderHelper;
import malte0811.controlengineering.render.tape.TapeDrive;
import malte0811.controlengineering.render.utils.PiecewiseAffinePath;
import malte0811.controlengineering.render.utils.PiecewiseAffinePath.Node;
import malte0811.controlengineering.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.tiles.panels.PanelCNCTile;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class PanelCNCRenderer extends TileEntityRenderer<PanelCNCTile> {
    //TODO reset?
    private static final ResettableLazy<TextureAtlasSprite> MODEL_TEXTURE = new ResettableLazy<>(
            () -> {
                AtlasTexture atlas = Minecraft.getInstance().getModelManager().getAtlasTexture(
                        PlayerContainer.LOCATION_BLOCKS_TEXTURE
                );
                return atlas.getSprite(new ResourceLocation(ControlEngineering.MODID, "block/panel_cnc"));
            }
    );

    private static final double HEAD_SIZE = 0.5;
    private static final double HEAD_TRAVERSAL_HEIGHT = 3;
    private static final double HEAD_WORK_HEIGHT = 1;
    private static final Vector3d HEAD_IDLE = new Vector3d(8 - HEAD_SIZE / 2, 5, 8 - HEAD_SIZE / 2);

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
        //TODO hasPanel => isActive
        final double tick = tile.getCurrentTicksInJob() + (tile.hasPanel() ? partialTicks : 0);
        transform.push();
        transform.translate(0, 14, 0);
        renderTape(tile, buffers, transform, light, overlay, tick);
        transform.pop();
        transform.push();
        transform.translate(1, 2, 1);
        transform.scale(14f / 16, 14f / 16, 14f / 16);
        renderCurrentPanelState(tile, buffers, transform, light, overlay);
        renderHead(tile, buffers, transform, light, overlay, tick);
        transform.pop();
        transform.pop();
    }

    private void rotateAroundCenter(double angleDegrees, MatrixStack stack) {
        stack.translate(.5, .5, .5);
        stack.rotate(new Quaternion(0, (float) angleDegrees, 0, true));
        stack.translate(-.5, -.5, -.5);
    }

    private void renderCurrentPanelState(
            PanelCNCTile tile, IRenderTypeBuffer buffers, MatrixStack transform, int light, int overlay
    ) {
        IVertexBuilder builder = buffers.getBuffer(RenderType.getSolid());
        if (tile.hasPanel()) {
            IVertexBuilder forTexture = MODEL_TEXTURE.get().wrapBuffer(builder);
            TransformingVertexBuilder finalWrapped = new TransformingVertexBuilder(forTexture, transform);
            finalWrapped.setColor(-1).setLight(light).setNormal(0, 1, 0).setOverlay(overlay);
            final float minU = 17 / 64f;
            final float maxU = 31 / 64f;
            final float minV = 1 - 1 / 32f;
            final float maxV = 1 - 15 / 32f;
            finalWrapped.pos(0, 0, 0).tex(minU, minV).endVertex();
            finalWrapped.pos(0, 0, 16).tex(minU, maxV).endVertex();
            finalWrapped.pos(16, 0, 16).tex(maxU, maxV).endVertex();
            finalWrapped.pos(16, 0, 0).tex(maxU, minV).endVertex();
        }
        for (PlacedComponent placed : tile.getCurrentPlacedComponents()) {
            PanelRenderer.renderComponent(transform, placed, builder, light, overlay);
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

    private void renderHead(
            PanelCNCTile tile, IRenderTypeBuffer buffer, MatrixStack transform, int light, int overlay, double ticks
    ) {
        Vector3d currentPos;
        if (tile.getCurrentJob() != null) {
            //TODO cache path!
            currentPos = createPathFor(tile.getCurrentJob()).getPosAt(ticks);
        } else {
            currentPos = HEAD_IDLE;
        }
        transform.push();
        transform.translate(currentPos.x, currentPos.y, currentPos.z);
        new RenderHelper(buffer.getBuffer(RenderType.getSolid()), light, overlay)
                .renderColoredQuad(
                        transform,
                        Vector3d.ZERO,
                        new Vector3d(0, 0, HEAD_SIZE),
                        new Vector3d(HEAD_SIZE, 0, HEAD_SIZE),
                        new Vector3d(HEAD_SIZE, 0, 0),
                        new Vector3d(0, 1, 0),
                        0xff00ff00,
                        OptionalInt.empty()
                );
        transform.pop();
    }

    private PiecewiseAffinePath<Vector3d> createPathFor(PanelCNCTile.CNCJob job) {
        final double arrival = 0.5;
        final double down = arrival + 1 / 16.;
        final double done = 15 / 16.;
        final double up = 1;
        List<Node<Vector3d>> nodes = new ArrayList<>();
        int lastComponentTime = 0;
        nodes.add(new Node<>(HEAD_IDLE, lastComponentTime));
        for (int i = 0; i < job.getTotalComponents(); ++i) {
            final PlacedComponent nextComponent = job.getComponents().get(i);
            final Vec2d min = nextComponent.getPosMin();
            final Vec2d max = nextComponent.getPosMax().subtract(new Vec2d(HEAD_SIZE, HEAD_SIZE));
            final int nextComponentTime = job.getTickPlacingComponent().getInt(i);
            final double arrivalAtComponent = MathHelper.lerp(arrival, lastComponentTime, nextComponentTime);
            nodes.add(new Node<>(new Vector3d(min.x, HEAD_TRAVERSAL_HEIGHT, min.y), arrivalAtComponent));
            final double downAtComp = MathHelper.lerp(down, lastComponentTime, nextComponentTime);
            final double doneAtComp = MathHelper.lerp(done, lastComponentTime, nextComponentTime);
            Vec2d[] corners = {min, new Vec2d(min.x, max.y), max, new Vec2d(max.x, min.y), min};
            for (int point = 0; point < corners.length; point++) {
                final double cornerTime = MathHelper.lerp(
                        point / (double) (corners.length - 1), downAtComp, doneAtComp
                );
                nodes.add(new Node<>(
                        new Vector3d(corners[point].x, HEAD_WORK_HEIGHT, corners[point].y), cornerTime
                ));
            }
            final double upAtComp = MathHelper.lerp(up, lastComponentTime, nextComponentTime);
            nodes.add(new Node<>(new Vector3d(min.x, HEAD_TRAVERSAL_HEIGHT, min.y), upAtComp));
            lastComponentTime = nextComponentTime;
        }
        nodes.add(new Node<>(HEAD_IDLE, job.getTotalTicks()));
        return new PiecewiseAffinePath<>(nodes, Vector3d::scale, Vector3d::add);
    }
}
