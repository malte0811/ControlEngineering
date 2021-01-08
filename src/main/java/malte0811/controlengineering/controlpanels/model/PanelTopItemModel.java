package malte0811.controlengineering.controlpanels.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.controlpanels.renders.target.StaticRenderTarget;
import malte0811.controlengineering.controlpanels.renders.target.TargetType;
import malte0811.controlengineering.items.PanelTopItem;
import malte0811.controlengineering.render.PanelRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PanelTopItemModel implements IBakedModel {
    private final ItemOverrideList overrideList = new OverrideList();

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return overrideList;
    }

    private static class OverrideList extends ItemOverrideList {
        private final LoadingCache<List<PlacedComponent>, IBakedModel> CACHED_MODELS = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(60, TimeUnit.SECONDS)
                .build(new Loader());

        @Nullable
        @Override
        public IBakedModel getOverrideModel(
                @Nonnull IBakedModel model,
                @Nonnull ItemStack stack,
                @Nullable ClientWorld world,
                @Nullable LivingEntity livingEntity
        ) {
            try {
                return CACHED_MODELS.get(PanelTopItem.getComponentsOn(stack));
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Loader extends CacheLoader<List<PlacedComponent>, IBakedModel> {
        @SuppressWarnings("deprecation")
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

        @Override
        public IBakedModel load(@Nonnull List<PlacedComponent> components) {
            MatrixStack transform = new MatrixStack();
            StaticRenderTarget target = new StaticRenderTarget($ -> true);
            TextureAtlasSprite panelTexture = PanelRenderer.PANEL_TEXTURE.get();
            target.renderTexturedQuad(
                    transform, panelTexture,
                    new Vector3d(0, 0, 0),
                    new Vector3d(0, 0, 1),
                    new Vector3d(1, 0, 1),
                    new Vector3d(1, 0, 0),
                    new Vector3d(0, 1, 0),
                    -1, OptionalInt.empty(), TargetType.STATIC
            );
            target.renderTexturedQuad(
                    transform, panelTexture,
                    new Vector3d(0, 0, 0),
                    new Vector3d(1, 0, 0),
                    new Vector3d(1, 0, 1),
                    new Vector3d(0, 0, 1),
                    new Vector3d(0, -1, 0),
                    -1, OptionalInt.empty(), TargetType.STATIC
            );
            transform.push();
            transform.scale(1 / 16f, 1 / 16f, 1 / 16f);
            ComponentRenderers.renderAll(target, components, transform);
            transform.pop();

            return new SimpleBakedModel(
                    target.getQuads(), StaticRenderTarget.EMPTY_LISTS_ON_ALL_SIDES, true, true, true,
                    panelTexture, TRANSFORMS, ItemOverrideList.EMPTY
            );
        }
    }
}
