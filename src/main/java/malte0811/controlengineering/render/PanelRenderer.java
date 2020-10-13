package malte0811.controlengineering.render;

import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.render.utils.DelegatingVertexBuilder;
import malte0811.controlengineering.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SpriteAwareVertexBuilder;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;

//TODO baked model? VBO? At least partially?
public class PanelRenderer extends TileEntityRenderer<ControlPanelTile> {
    //TODO reset
    private static final ResettableLazy<TextureAtlasSprite> texture = new ResettableLazy<>(
            () -> {
                ResourceLocation loc = new ResourceLocation(ControlEngineering.MODID, "block/control_panel");
                return Minecraft.getInstance()
                        .getModelManager()
                        .getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
                        .getSprite(loc);
            }
    );

    public PanelRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(
            ControlPanelTile tile,
            float partialTicks,
            @Nonnull MatrixStack transform,
            @Nonnull IRenderTypeBuffer buffer,
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
        IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
        renderPanel(tile, transform, builder, texture.get(), combinedLight, combinedOverlay);
        tile.getTransform().getPanelTopToWorld().toTransformationMatrix().push(transform);
        final float baseScale = 1 / 16f;
        transform.translate(0, 1e-3, 0);
        transform.scale(baseScale, baseScale, baseScale);
        for (PlacedComponent comp : tile.getComponents()) {
            transform.push();
            transform.translate(comp.getPos().x, 0, comp.getPos().y);
            ComponentRenderers.render(builder, comp.getComponent(), transform, combinedLight, combinedOverlay);
            transform.pop();
        }
        transform.pop();
    }

    private static void renderPanel(
            ControlPanelTile panel,
            MatrixStack matrix,
            IVertexBuilder builder,
            TextureAtlasSprite texture,
            int light,
            int overlay
    ) {
        Vector3d[] bottomVertices = layerVertices(1);
        Vector3d[] topVertices = layerVertices(panel.getTransform().getTopFaceHeight());
        for (int i = 0; i < 4; ++i) {
            bottomVertices[i] = panel.getTransform().getPanelBottomToWorld().apply(bottomVertices[i]);
            topVertices[i] = panel.getTransform().getPanelTopToWorld().apply(topVertices[i]);
        }
        TransformingVertexBuilder goodBuilder = new TransformingVertexBuilder(
                new SpriteAwareVertexBuilder(builder, texture), matrix
        );
        goodBuilder.setOverlay(overlay);
        goodBuilder.setLight(light);
        goodBuilder.setColor(1, 1, 1, 1);
        renderQuad(topVertices, goodBuilder);
        final double frontHeight = panel.getTransform().getFrontHeight();
        final double backHeight = panel.getTransform().getBackHeight();
        renderConnections(bottomVertices, topVertices, goodBuilder, new double[]{
                frontHeight, backHeight, backHeight, frontHeight
        });
    }

    //TODO deduplicate with PanelClientEvents?
    private static void renderQuad(Vector3d[] vertices, TransformingVertexBuilder builderIn) {
        Preconditions.checkArgument(vertices.length == 4);
        Vector3d normal = normal(vertices[0], vertices[1], vertices[3]);
        builderIn.setNormal(normal);
        quad(builderIn, vertices[3], vertices[2], vertices[1], vertices[0], 0, 1, 1, 0);
    }

    private static Vector3d normal(Vector3d a, Vector3d b, Vector3d c) {
        return a.subtract(b).crossProduct(a.subtract(c)).normalize();
    }

    private static void renderConnections(
            Vector3d[] first,
            Vector3d[] second,
            TransformingVertexBuilder builder,
            double[] height
    ) {
        Preconditions.checkArgument(first.length == second.length);
        for (int i = 0; i < first.length; ++i) {
            int next = (i + 1) % first.length;
            quad(builder, first[i], second[i], second[next], first[next], 0, height[i], height[next], 0);
        }
    }

    private static void quad(
            DelegatingVertexBuilder<?> builder,
            Vector3d v1,
            Vector3d v2,
            Vector3d v3,
            Vector3d v4,
            double tex1,
            double tex2,
            double tex3,
            double tex4
    ) {
        builder.pos(v1).tex(0, (float) tex1).endVertex();
        builder.pos(v2).tex(0, (float) tex2).endVertex();
        builder.pos(v3).tex(1, (float) tex3).endVertex();
        builder.pos(v4).tex(1, (float) tex4).endVertex();
    }

    private static Vector3d[] layerVertices(double xMax) {
        return new Vector3d[]{
                new Vector3d(0, 0, 0),
                new Vector3d(xMax, 0, 0),
                new Vector3d(xMax, 0, 1),
                new Vector3d(0, 0, 1),
        };
    }
}
