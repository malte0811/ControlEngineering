package malte0811.controlengineering.controlpanels.model;

import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.controlpanels.PanelData;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.controlpanels.renders.target.QuadBuilder;
import malte0811.controlengineering.controlpanels.renders.target.RenderTarget;
import malte0811.controlengineering.controlpanels.renders.target.StaticRenderTarget;
import malte0811.controlengineering.controlpanels.renders.target.TargetType;
import malte0811.controlengineering.render.PanelRenderer;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class PanelModel implements IBakedModel {
    private static final ModelProperty<PanelData> COMPONENTS = new ModelProperty<>($ -> true);

    private final ItemOverrideList overrideList = new OverrideList();
    private final LoadingCache<PanelData, IBakedModel> CACHED_MODELS = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .build(new Loader(TargetType.STATIC::equals));

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
        return getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData
    ) {
        return getModel(CACHED_MODELS, extraData.getData(COMPONENTS)).getQuads(state, side, rand, extraData);
    }

    private static IBakedModel getModel(
            LoadingCache<PanelData, IBakedModel> cache, @Nullable PanelData list
    ) {
        if (list == null) {
            list = new PanelData();
        }
        //TODO mutability issues! Might not actually be a problem though, since components are recreated on NBT read?
        try {
            return cache.get(list);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData(
            @Nonnull IBlockDisplayReader world,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull IModelData tileData
    ) {
        final ControlPanelTile tile = PanelBlock.getBase(world, state, pos);
        if (tile == null) {
            return tileData;
        }
        final List<PlacedComponent> components = tile.getComponents();
        final PanelTransform transform = tile.getTransform();
        return CombinedModelData.combine(
                tileData, new SinglePropertyModelData<>(new PanelData(components, transform), COMPONENTS)
        );
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
        return PanelRenderer.PANEL_TEXTURE.get();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return overrideList;
    }

    private static class OverrideList extends ItemOverrideList {
        private final LoadingCache<PanelData, IBakedModel> CACHED_MODELS = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(60, TimeUnit.SECONDS)
                .build(new Loader($ -> true));

        @Nullable
        @Override
        public IBakedModel getOverrideModel(
                @Nonnull IBakedModel model,
                @Nonnull ItemStack stack,
                @Nullable ClientWorld world,
                @Nullable LivingEntity livingEntity
        ) {
            try {
                CompoundNBT tag = stack.getTag();
                if (tag == null) {
                    tag = new CompoundNBT();
                }
                return CACHED_MODELS.get(new PanelData(tag, PanelOrientation.UP_NORTH));
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
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
            Vector3d[] bottomVertices = layerVertices(1);
            Vector3d[] topVertices = layerVertices(transform.getTopFaceHeight());
            for (int i = 0; i < 4; ++i) {
                bottomVertices[i] = transform.getPanelBottomToWorld().apply(bottomVertices[i]);
                topVertices[i] = transform.getPanelTopToWorld().apply(topVertices[i]);
            }
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

        private static Vector3d[] layerVertices(double xMax) {
            return new Vector3d[]{
                    new Vector3d(0, 0, 0),
                    new Vector3d(xMax, 0, 0),
                    new Vector3d(xMax, 0, 1),
                    new Vector3d(0, 0, 1),
            };
        }
    }
}
