package malte0811.controlengineering.client.model.panel;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import malte0811.controlengineering.client.render.panel.PanelRenderer;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.utils.BakedQuadVertexBuilder;
import malte0811.controlengineering.controlpanels.PanelData;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

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
    private final LoadingCache<PanelData, BakedModel> cachedStaticModels;
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

    public BakedModel getStaticModel(@Nullable PanelData list) {
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
            PoseStack transform = new PoseStack();
            new Transformation(cacheKey.getTransform().getPanelTopToWorld()).push(transform);
            transform.scale(1 / 16f, 1 / 16f, 1 / 16f);
            return ComponentRenderers.renderAll(cacheKey.getComponents(), transform, staticTypes);
        }
    }

    private static class StaticLoader extends CacheLoader<PanelData, BakedModel> {
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
        public BakedModel load(@Nonnull PanelData cacheKey) {
            MixedModel mixed = getMixedModel.apply(cacheKey);
            List<BakedQuad> quads = new ArrayList<>(mixed.getStaticQuads());
            PoseStack transform = new PoseStack();
            TextureAtlasSprite panelTexture = PanelRenderer.PANEL_TEXTURE.get();
            renderPanel(cacheKey.getTransform(), BakedQuadVertexBuilder.makeInterpolating(panelTexture, transform, quads));
            // TODO render type
            return new SimpleBakedModel(
                    quads, EMPTY_LISTS_ON_ALL_SIDES, true, true, true,
                    panelTexture, Transforms.PANEL_TRANSFORMS, ItemOverrides.EMPTY
            );
        }
    }

    public static void renderPanel(PanelTransform transform, VertexConsumer builder) {
        TextureAtlasSprite texture = PanelRenderer.PANEL_TEXTURE.get();
        Vec3[] bottomVertices = transform.getBottomVertices();
        Vec3[] topVertices = transform.getTopVertices();
        new QuadBuilder(topVertices[3], topVertices[2], topVertices[1], topVertices[0])
                .setSprite(texture)
                .writeTo(builder);
        new QuadBuilder(bottomVertices[0], bottomVertices[1], bottomVertices[2], bottomVertices[3])
                .setSprite(texture)
                .setNormal(new Vec3(0, -1, 0))
                .writeTo(builder);
        final double frontHeight = transform.getFrontHeight();
        final double backHeight = transform.getBackHeight();
        renderConnections(builder, texture, bottomVertices, topVertices, new double[]{
                frontHeight, backHeight, backHeight, frontHeight
        });
    }

    private static void renderConnections(
            VertexConsumer builder, TextureAtlasSprite texture,
            Vec3[] first, Vec3[] second, double[] height
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
