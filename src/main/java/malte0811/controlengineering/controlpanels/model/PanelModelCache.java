package malte0811.controlengineering.controlpanels.model;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.utils.BakedQuadVertexBuilder;
import malte0811.controlengineering.controlpanels.PanelData;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.controlpanels.renders.PanelRenderer;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class PanelModelCache {
    private final LoadingCache<PanelData, IBakedModel> cachedStaticModels;
    private final LoadingCache<PanelData, MixedModel> componentModels;

    public PanelModelCache(RenderType... staticTypes) {
        componentModels = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(60, TimeUnit.SECONDS)
                .build(new MixedLoader(staticTypes));
        cachedStaticModels = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(60, TimeUnit.SECONDS)
                .build(new StaticLoader(componentModels));
    }

    public IBakedModel getStaticModel(@Nullable PanelData list) {
        return getFromCache(cachedStaticModels, list);
    }

    public MixedModel getMixedModel(@Nullable PanelData list) {
        return getFromCache(componentModels, list);
    }

    public void clear() {
        cachedStaticModels.invalidateAll();
        componentModels.invalidateAll();
    }

    private static <T> T getFromCache(LoadingCache<PanelData, T> cache, @Nullable PanelData list) {
        if (list == null) {
            list = new PanelData();
        }
        try {
            T result = cache.getIfPresent(list);
            if (result == null) {
                result = cache.get(list.copy(false));
            }
            return result;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static class MixedLoader extends CacheLoader<PanelData, MixedModel> {
        private final RenderType[] staticTypes;

        MixedLoader(RenderType... staticTypes) {
            this.staticTypes = staticTypes;
        }

        @Override
        public MixedModel load(@Nonnull PanelData cacheKey) {
            MatrixStack transform = new MatrixStack();
            cacheKey.getTransform().getPanelTopToWorld().toTransformationMatrix().push(transform);
            transform.scale(1 / 16f, 1 / 16f, 1 / 16f);
            return ComponentRenderers.renderAll(cacheKey.getComponents(), transform, staticTypes);
        }
    }

    private static class StaticLoader extends CacheLoader<PanelData, IBakedModel> {
        public static final Map<Direction, List<BakedQuad>> EMPTY_LISTS_ON_ALL_SIDES = Util.make(
                new EnumMap<>(Direction.class),
                m -> {
                    for (Direction d : DirectionUtils.VALUES) {
                        m.put(d, ImmutableList.of());
                    }
                }
        );

        private final Function<PanelData, MixedModel> getMixedModel;

        public StaticLoader(Function<PanelData, MixedModel> getMixedModel) {
            this.getMixedModel = getMixedModel;
        }

        @Override
        public IBakedModel load(@Nonnull PanelData cacheKey) {
            MixedModel mixed = getMixedModel.apply(cacheKey);
            List<BakedQuad> quads = new ArrayList<>(mixed.getStaticQuads());
            MatrixStack transform = new MatrixStack();
            TextureAtlasSprite panelTexture = PanelRenderer.PANEL_TEXTURE.get();
            renderPanel(cacheKey.getTransform(), new BakedQuadVertexBuilder(panelTexture, transform, quads));
            return new SimpleBakedModel(
                    quads, EMPTY_LISTS_ON_ALL_SIDES, true, true, true,
                    panelTexture, Transforms.PANEL_TRANSFORMS, ItemOverrideList.EMPTY
            );
        }
    }

    public static void renderPanel(PanelTransform transform, IVertexBuilder builder) {
        TextureAtlasSprite texture = PanelRenderer.PANEL_TEXTURE.get();
        Vector3d[] bottomVertices = transform.getBottomVertices();
        Vector3d[] topVertices = transform.getTopVertices();
        new QuadBuilder(topVertices[3], topVertices[2], topVertices[1], topVertices[0])
                .setSprite(texture)
                .writeTo(builder);
        new QuadBuilder(bottomVertices[0], bottomVertices[1], bottomVertices[2], bottomVertices[3])
                .setSprite(texture)
                .setNormal(new Vector3d(0, -1, 0))
                .writeTo(builder);
        final double frontHeight = transform.getFrontHeight();
        final double backHeight = transform.getBackHeight();
        renderConnections(builder, texture, bottomVertices, topVertices, new double[]{
                frontHeight, backHeight, backHeight, frontHeight
        });
    }

    private static void renderConnections(
            IVertexBuilder builder, TextureAtlasSprite texture,
            Vector3d[] first, Vector3d[] second, double[] height
    ) {
        Preconditions.checkArgument(first.length == second.length);
        for (int i = 0; i < first.length; ++i) {
            int next = (i + 1) % first.length;
            new QuadBuilder(first[i], second[i], second[next], first[next])
                    .setSprite(texture)
                    .setVCoords(0, (float) height[i], (float) height[next], 0)
                    .writeTo(builder);
        }
    }
}
