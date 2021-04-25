package malte0811.controlengineering.client.render;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.target.RenderTarget;
import malte0811.controlengineering.client.render.target.StaticRenderTarget;
import malte0811.controlengineering.client.render.target.TargetType;
import malte0811.controlengineering.controlpanels.PanelData;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class PanelModelCache {
    private final LoadingCache<PanelData, IBakedModel> cachedModels;

    public PanelModelCache(Predicate<TargetType> targets) {
        cachedModels = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(60, TimeUnit.SECONDS)
                .build(new Loader(targets));
    }

    public IBakedModel getModel(@Nullable PanelData list) {
        if (list == null) {
            list = new PanelData();
        }
        try {
            IBakedModel result = cachedModels.getIfPresent(list);
            if (result == null) {
                result = cachedModels.get(list.copy());
            }
            return result;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static class Loader extends CacheLoader<PanelData, IBakedModel> {
        @SuppressWarnings("deprecation")
        //TODO adjust
        private static final ItemCameraTransforms TRANSFORMS = new ItemCameraTransforms(
                new ItemTransformVec3f(
                        new Vector3f(75, 225, 0),
                        new Vector3f(0, 0, 0.125f),
                        new Vector3f(0.375F, 0.375F, 0.375F)
                ),
                new ItemTransformVec3f(
                        new Vector3f(75, 45, 0),
                        new Vector3f(0, 0, 0.125f),
                        new Vector3f(0.375F, 0.375F, 0.375F)
                ),
                new ItemTransformVec3f(new Vector3f(0, 225, 0), new Vector3f(1, 0, -1), new Vector3f(1, 1, 1)),
                new ItemTransformVec3f(new Vector3f(0, 45, 0), new Vector3f(1, 0, -1), new Vector3f(1, 1, 1)),
                ItemTransformVec3f.DEFAULT,
                new ItemTransformVec3f(
                        new Vector3f(30, 225, 0),
                        new Vector3f(0, 0, 0),
                        new Vector3f(0.625F, 0.625F, 0.625F)
                ),
                new ItemTransformVec3f(
                        new Vector3f(0, 0, 0),
                        new Vector3f(0, .375f, 0),
                        new Vector3f(0.25f, 0.25f, 0.25f)
                ),
                new ItemTransformVec3f(new Vector3f(-90, 0, 0), new Vector3f(0, 0, -.5f), new Vector3f(1, 1, 1))
        );

        private final Predicate<TargetType> includedTargets;

        private Loader(Predicate<TargetType> includedTargets) {
            this.includedTargets = includedTargets;
        }

        @Override
        public IBakedModel load(@Nonnull PanelData cacheKey) {
            MatrixStack transform = new MatrixStack();
            StaticRenderTarget target = new StaticRenderTarget(includedTargets);
            TextureAtlasSprite panelTexture = PanelRenderer.PANEL_TEXTURE.get();
            renderPanel(cacheKey.getTransform(), transform, target, panelTexture);
            cacheKey.getTransform().getPanelTopToWorld().toTransformationMatrix().push(transform);
            transform.scale(1 / 16f, 1 / 16f, 1 / 16f);
            ComponentRenderers.renderAll(target, cacheKey.getComponents(), transform);
            transform.pop();
            return new SimpleBakedModel(
                    target.getQuads(), StaticRenderTarget.EMPTY_LISTS_ON_ALL_SIDES, true, true, true,
                    panelTexture, TRANSFORMS, ItemOverrideList.EMPTY
            );
        }

        private static void renderPanel(
                PanelTransform transform, MatrixStack matrix, RenderTarget builder, TextureAtlasSprite texture
        ) {
            Vector3d[] bottomVertices = transform.getBottomVertices();
            Vector3d[] topVertices = transform.getTopVertices();
            new QuadBuilder(topVertices[3], topVertices[2], topVertices[1], topVertices[0])
                    .setSprite(texture)
                    .writeTo(matrix, builder, TargetType.STATIC);
            new QuadBuilder(bottomVertices[0], bottomVertices[1], bottomVertices[2], bottomVertices[3])
                    .setSprite(texture)
                    .setNormal(new Vector3d(0, -1, 0))
                    .writeTo(matrix, builder, TargetType.SPECIAL);
            final double frontHeight = transform.getFrontHeight();
            final double backHeight = transform.getBackHeight();
            renderConnections(builder, matrix, texture, bottomVertices, topVertices, new double[]{
                    frontHeight, backHeight, backHeight, frontHeight
            });
        }

        private static void renderConnections(
                RenderTarget builder, MatrixStack transform, TextureAtlasSprite texture,
                Vector3d[] first, Vector3d[] second, double[] height
        ) {
            Preconditions.checkArgument(first.length == second.length);
            for (int i = 0; i < first.length; ++i) {
                int next = (i + 1) % first.length;
                new QuadBuilder(first[i], second[i], second[next], first[next])
                        .setSprite(texture)
                        .setVCoords(0, (float) height[i], (float) height[next], 0)
                        .writeTo(transform, builder, TargetType.STATIC);
            }
        }
    }
}
