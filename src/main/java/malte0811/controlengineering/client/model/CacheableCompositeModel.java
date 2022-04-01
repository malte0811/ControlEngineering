package malte0811.controlengineering.client.model;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry.ExpandedBlockModelDeserializer;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public record CacheableCompositeModel(
        List<ICacheKeyProvider<?>> cacheableSubModels, List<BakedQuad> simpleQuads, ModelState modelTransform
) implements CEBakedModel.Cacheable<List<?>> {
    private static final ModelProperty<List<IModelData>> SUB_MODEL_DATA = new ModelProperty<>();

    @Override
    public List<BakedQuad> getQuads(List<?> key) {
        if (cacheableSubModels.size() != key.size()) {
            return List.of();
        }
        List<BakedQuad> result = new ArrayList<>(simpleQuads);
        for (int i = 0; i < cacheableSubModels.size(); i++) {
            ICacheKeyProvider<?> cacheable = cacheableSubModels.get(i);
            result.addAll(getQuads(cacheable, key.get(i)));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <K> List<BakedQuad> getQuads(ICacheKeyProvider<K> cacheable, Object key) {
        return cacheable.getQuads((K) key);
    }

    @Override
    public List<?> getKey(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData
    ) {
        if (side != null) {
            return List.of();
        }
        var subProperties = extraData.getData(SUB_MODEL_DATA);
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < cacheableSubModels.size(); i++) {
            ICacheKeyProvider<?> cacheable = cacheableSubModels.get(i);
            result.add(cacheable.getKey(
                    state, null, rand, subProperties != null ? subProperties.get(i) : EmptyModelData.INSTANCE
            ));
        }
        return result;
    }

    @Nonnull
    @Override
    public IModelData getModelData(
            @Nonnull BlockAndTintGetter level,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull IModelData modelData
    ) {
        List<IModelData> subData = new ArrayList<>(cacheableSubModels.size());
        for (var cacheable : cacheableSubModels) {
            subData.add(cacheable.getModelData(level, pos, state, modelData));
        }
        return new SinglePropertyModelData<>(subData, SUB_MODEL_DATA);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull IModelData data) {
        if (!simpleQuads.isEmpty()) {
            return simpleQuads.get(0).getSprite();
        } else {
            return cacheableSubModels.get(0).getParticleIcon(data);
        }
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType type, PoseStack transform) {
        modelTransform.getPartTransformation(type).push(transform);
        return this;
    }

    private record Geometry(List<BlockModel> subModels) implements IModelGeometry<Geometry> {
        @Override
        public BakedModel bake(
                IModelConfiguration owner,
                ModelBakery bakery,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelTransform,
                ItemOverrides overrides,
                ResourceLocation modelLocation
        ) {
            var quads = new ArrayList<BakedQuad>();
            var bakedSubModels = new ArrayList<ICacheKeyProvider<?>>();
            for (var model : subModels) {
                var baked = model.bake(bakery, model, spriteGetter, modelTransform, modelLocation, true);
                if (baked instanceof SimpleBakedModel simple) {
                    quads.addAll(simple.getQuads(null, null, ApiUtils.RANDOM, EmptyModelData.INSTANCE));
                    for (var side : DirectionUtils.VALUES) {
                        quads.addAll(simple.getQuads(null, side, ApiUtils.RANDOM, EmptyModelData.INSTANCE));
                    }
                } else if (baked instanceof ICacheKeyProvider<?> cacheable) {
                    bakedSubModels.add(cacheable);
                } else {
                    throw new RuntimeException("Unexpected submodel " + baked);
                }
            }
            return new CacheableCompositeModel(bakedSubModels, quads, owner.getCombinedTransform());
        }

        @Override
        public Collection<Material> getTextures(
                IModelConfiguration owner,
                Function<ResourceLocation, UnbakedModel> modelGetter,
                Set<Pair<String, String>> missingTextureErrors
        ) {
            Set<Material> set = new HashSet<>();
            for (BlockModel bm : subModels) {
                set.addAll(bm.getMaterials(modelGetter, missingTextureErrors));
            }
            return set;
        }
    }

    public static class Loader implements IModelLoader<Geometry> {
        public static final String SUBMOCELS = "submodels";

        @Nonnull
        @Override
        public Geometry read(
                @Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents
        ) {
            var submodels = new ArrayList<BlockModel>();
            for (var submodel : modelContents.getAsJsonArray(SUBMOCELS)) {
                submodels.add(ExpandedBlockModelDeserializer.INSTANCE.fromJson(submodel, BlockModel.class));
            }
            return new Geometry(submodels);
        }

        @Override
        public void onResourceManagerReload(@Nonnull ResourceManager modelpResourceManager) {}
    }
}
