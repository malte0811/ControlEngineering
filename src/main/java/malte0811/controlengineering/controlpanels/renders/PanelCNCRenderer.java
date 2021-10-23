package malte0811.controlengineering.controlpanels.renders;

import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.client.render.tape.TapeDrive;
import malte0811.controlengineering.client.render.utils.ModelRenderUtils;
import malte0811.controlengineering.client.render.utils.PiecewiseAffinePath;
import malte0811.controlengineering.client.render.utils.PiecewiseAffinePath.Node;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.tiles.panels.CNCJob;
import malte0811.controlengineering.tiles.panels.PanelCNCTile;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PanelCNCRenderer implements BlockEntityRenderer<PanelCNCTile> {
    //TODO reset?
    private static final ResettableLazy<TextureAtlasSprite> MODEL_TEXTURE = new ResettableLazy<>(
            () -> {
                TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(
                        InventoryMenu.BLOCK_ATLAS
                );
                return atlas.getSprite(new ResourceLocation(ControlEngineering.MODID, "block/panel_cnc"));
            }
    );

    private static final double HEAD_SIZE = 0.5;
    private static final double HEAD_TRAVERSAL_HEIGHT = 3;
    private static final double HEAD_WORK_HEIGHT = -1;
    private static final Vec3 HEAD_IDLE = new Vec3(8 - HEAD_SIZE / 2, 5, 8 - HEAD_SIZE / 2);

    public PanelCNCRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(
            @Nonnull PanelCNCTile tile,
            float partialTicks,
            @Nonnull PoseStack transform,
            @Nonnull MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        transform.pushPose();
        rotateAroundCenter(-PanelCNCBlock.getDirection(tile).toYRot(), transform);
        transform.scale(1 / 16f, 1 / 16f, 1 / 16f);
        //TODO hasPanel => isActive
        final double tick = tile.getCurrentTicksInJob() + (tile.hasPanel() ? partialTicks : 0);
        transform.pushPose();
        transform.translate(0, 14, 0);
        renderTape(tile, buffers, transform, light, overlay, tick);
        transform.popPose();
        transform.pushPose();
        transform.translate(1, 2, 1);
        transform.scale(14f / 16, 14f / 16, 14f / 16);
        renderCurrentPanelState(tile, buffers, transform, light, overlay);
        renderHead(tile, buffers, transform, light, overlay, tick);
        transform.popPose();
        transform.popPose();
    }

    private void rotateAroundCenter(double angleDegrees, PoseStack stack) {
        stack.translate(.5, .5, .5);
        stack.mulPose(new Quaternion(0, (float) angleDegrees, 0, true));
        stack.translate(-.5, -.5, -.5);
    }

    private void renderCurrentPanelState(
            PanelCNCTile tile, MultiBufferSource buffers, PoseStack transform, int light, int overlay
    ) {
        VertexConsumer builder = buffers.getBuffer(RenderType.solid());
        if (tile.hasPanel()) {
            VertexConsumer forTexture = MODEL_TEXTURE.get().wrap(builder);
            TransformingVertexBuilder finalWrapped = new TransformingVertexBuilder(
                    forTexture, transform, DefaultVertexFormat.BLOCK
            );
            finalWrapped.setColor(-1).setLight(light).setNormal(0, 1, 0).setOverlay(overlay);
            final float minU = 17 / 64f;
            final float maxU = 31 / 64f;
            final float minV = 31 / 32f;
            final float maxV = 17 / 32f;
            finalWrapped.vertex(0, 0, 0).uv(minU, minV).endVertex();
            finalWrapped.vertex(0, 0, 16).uv(minU, maxV).endVertex();
            finalWrapped.vertex(16, 0, 16).uv(maxU, maxV).endVertex();
            finalWrapped.vertex(16, 0, 0).uv(maxU, minV).endVertex();
        }
        //TODO cache?
        ComponentRenderers.renderAll(tile.getCurrentPlacedComponents(), new PoseStack())
                .renderTo(buffers, transform, light, overlay);
    }

    private void renderTape(
            PanelCNCTile tile, MultiBufferSource buffer, PoseStack transform, int light, int overlay, double ticks
    ) {
        final long totLength = tile.getTapeLength();
        if (totLength > 0) {
            CNCJob currentJob = tile.getCurrentJob();
            Preconditions.checkNotNull(currentJob);
            //TODO put into TE in some way? Or make static(ish)?
            TapeDrive testWheel = new TapeDrive(
                    totLength + 1, 2, 1,
                    new Vec2d(5, 8), new Vec2d(7, 5),
                    new Vec2d(11, 8), new Vec2d(9, 5)
            );
            testWheel.updateTapeProgress(currentJob.getTapeProgressAtTime(ticks));
            testWheel.render(buffer.getBuffer(RenderType.solid()), transform, light, overlay);
        }
    }

    private void renderHead(
            PanelCNCTile tile, MultiBufferSource buffer, PoseStack transform, int light, int overlay, double ticks
    ) {
        Vec3 currentPos;
        if (tile.getCurrentJob() != null && !tile.hasFailed()) {
            //TODO cache path!
            currentPos = createPathFor(tile.getCurrentJob()).getPosAt(ticks);
        } else {
            currentPos = HEAD_IDLE;
        }
        transform.pushPose();
        transform.translate(currentPos.x, 0, currentPos.z);
        //TODO pull out
        VertexConsumer solidBuffer = buffer.getBuffer(RenderType.solid());
        VertexConsumer forTexture = MODEL_TEXTURE.get().wrap(solidBuffer);
        TransformingVertexBuilder innerBuilder = new TransformingVertexBuilder(
                forTexture, transform, DefaultVertexFormat.BLOCK
        );
        innerBuilder.setOverlay(overlay)
                .setLight(light)
                .setColor(-1);
        renderHeadModel(innerBuilder, (float) currentPos.y);
        transform.popPose();
    }

    private void renderHeadModel(TransformingVertexBuilder builder, float yMin) {
        final float yMax = 12;
        final float headHeight = 1;
        final ModelRenderUtils.UVCoord tipMin = new ModelRenderUtils.UVCoord(36 / 64f, 12 / 32f);
        final ModelRenderUtils.UVCoord tipMax = new ModelRenderUtils.UVCoord(40 / 64f, 14 / 32f);
        ModelRenderUtils.renderTube(builder, 0, HEAD_SIZE, yMin, yMin + headHeight, tipMin, tipMax);
        final ModelRenderUtils.UVCoord shaftMin = new ModelRenderUtils.UVCoord(36 / 64f, 12 / 32f);
        final float shaftLength = yMax - yMin - headHeight;
        final ModelRenderUtils.UVCoord shaftMax = new ModelRenderUtils.UVCoord(40 / 64f, (12 - shaftLength) / 32f);
        ModelRenderUtils.renderTube(builder, HEAD_SIZE, HEAD_SIZE, yMin + headHeight, yMax, shaftMin, shaftMax);
    }

    private PiecewiseAffinePath<Vec3> createPathFor(CNCJob job) {
        final double arrival = 0.5;
        final double down = arrival + 1 / 16.;
        final double done = 15 / 16.;
        final double up = 1;
        List<Node<Vec3>> nodes = new ArrayList<>();
        int lastComponentTime = 0;
        nodes.add(new Node<>(HEAD_IDLE, lastComponentTime));
        for (int i = 0; i < job.getTotalComponents(); ++i) {
            final PlacedComponent nextComponent = job.components().get(i);
            final Vec2d min = nextComponent.getPosMin();
            final Vec2d max = nextComponent.getPosMax().subtract(new Vec2d(HEAD_SIZE, HEAD_SIZE));
            final int nextComponentTime = job.tickPlacingComponent().getInt(i);
            final double arrivalAtComponent = Mth.lerp(arrival, lastComponentTime, nextComponentTime);
            nodes.add(new Node<>(new Vec3(min.x(), HEAD_TRAVERSAL_HEIGHT, min.y()), arrivalAtComponent));
            final double downAtComp = Mth.lerp(down, lastComponentTime, nextComponentTime);
            final double doneAtComp = Mth.lerp(done, lastComponentTime, nextComponentTime);
            Vec2d[] corners = {min, new Vec2d(min.x(), max.y()), max, new Vec2d(max.x(), min.y()), min};
            for (int point = 0; point < corners.length; point++) {
                final double cornerTime = Mth.lerp(
                        point / (double) (corners.length - 1), downAtComp, doneAtComp
                );
                nodes.add(new Node<>(
                        new Vec3(corners[point].x(), HEAD_WORK_HEIGHT, corners[point].y()), cornerTime
                ));
            }
            final double upAtComp = Mth.lerp(up, lastComponentTime, nextComponentTime);
            nodes.add(new Node<>(new Vec3(min.x(), HEAD_TRAVERSAL_HEIGHT, min.y()), upAtComp));
            lastComponentTime = nextComponentTime;
        }
        nodes.add(new Node<>(HEAD_IDLE, job.totalTicks()));
        return new PiecewiseAffinePath<>(nodes, Vec3::scale, Vec3::add);
    }
}
