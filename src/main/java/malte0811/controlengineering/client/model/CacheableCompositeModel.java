package malte0811.controlengineering.client.model;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.client.ModelDataUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.client.render.target.RenderUtils;
import net.minecraft.client.renderer.RenderType;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public record CacheableCompositeModel(
        List<ICacheKeyProvider<?>> cacheableSubModels,
        List<BakedQuad> simpleQuads,
        ChunkRenderTypeSet simpleRenderTypes,
        ItemTransforms modelTransform
) implements CEBakedModel.Cacheable<List<?>> {
    private static final ModelProperty<List<ModelData>> SUB_MODEL_DATA = new ModelProperty<>();

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
            @Nullable BlockState state,
            @Nullable Direction side,
            @Nonnull RandomSource rand,
            @Nonnull ModelData extraData,
            @Nullable RenderType layer
    ) {
        if (side != null) {
            return List.of();
        }
        var subProperties = extraData.get(SUB_MODEL_DATA);
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < cacheableSubModels.size(); i++) {
            ICacheKeyProvider<?> cacheable = cacheableSubModels.get(i);
            result.add(cacheable.getKey(
                    state, null, rand, subProperties != null ? subProperties.get(i) : ModelData.EMPTY, layer
            ));
        }
        return result;
    }

    @Nonnull
    @Override
    public ModelData getModelData(
            @Nonnull BlockAndTintGetter level,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull ModelData modelData
    ) {
        List<ModelData> subData = new ArrayList<>(cacheableSubModels.size());
        for (var cacheable : cacheableSubModels) {
            subData.add(cacheable.getModelData(level, pos, state, modelData));
        }
        return ModelDataUtils.single(SUB_MODEL_DATA, subData);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data) {
        if (!simpleQuads.isEmpty()) {
            return simpleQuads.get(0).getSprite();
        } else {
            return cacheableSubModels.get(0).getParticleIcon(data);
        }
    }

    @Nonnull
    @Override
    public BakedModel applyTransform(
            @Nonnull ItemTransforms.TransformType transformType,
            @Nonnull PoseStack transform,
            boolean applyLeftHandTransform
    ) {
        modelTransform.getTransform(transformType).apply(applyLeftHandTransform, transform);
        return this;
    }

    @Nonnull
    @Override
    public ChunkRenderTypeSet getRenderTypes(
            @NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data
    ) {
        List<ChunkRenderTypeSet> children = new ArrayList<>();
        for (var child : cacheableSubModels) {
            children.add(child.getRenderTypes(state, rand, data));
        }
        children.add(simpleRenderTypes);
        return ChunkRenderTypeSet.union(children);
    }

    private record Geometry(List<BlockModel> subModels) implements IUnbakedGeometry<Geometry> {
        @Override
        public BakedModel bake(
                IGeometryBakingContext owner,
                ModelBakery bakery,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelTransform,
                ItemOverrides overrides,
                ResourceLocation modelLocation
        ) {
            var quads = new ArrayList<BakedQuad>();
            var renderTypes = new ArrayList<ChunkRenderTypeSet>();
            var bakedSubModels = new ArrayList<ICacheKeyProvider<?>>();
            for (var model : subModels) {
                var baked = model.bake(bakery, model, spriteGetter, modelTransform, modelLocation, true);
                if (baked instanceof SimpleBakedModel simple) {
                    quads.addAll(simple.getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null));
                    for (var side : DirectionUtils.VALUES) {
                        quads.addAll(simple.getQuads(null, side, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null));
                    }
                    renderTypes.add(simple.getRenderTypes(
                            Blocks.AIR.defaultBlockState(), ApiUtils.RANDOM_SOURCE, ModelData.EMPTY
                    ));
                } else if (baked instanceof ICacheKeyProvider<?> cacheable) {
                    bakedSubModels.add(cacheable);
                } else {
                    throw new RuntimeException("Unexpected submodel " + baked);
                }
            }
            return new CacheableCompositeModel(
                    bakedSubModels, quads, ChunkRenderTypeSet.union(renderTypes), owner.getTransforms()
            );
        }

        @Override
        public Collection<Material> getMaterials(
                IGeometryBakingContext owner,
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

    public static class Loader implements IGeometryLoader<Geometry> {
        public static final String SUBMOCELS = "submodels";

        @Nonnull
        @Override
        public Geometry read(
                @Nonnull JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext
        ) {
            var submodels = new ArrayList<BlockModel>();
            for (var submodel : modelContents.getAsJsonArray(SUBMOCELS)) {
                submodels.add(ExtendedBlockModelDeserializer.INSTANCE.fromJson(submodel, BlockModel.class));
            }
            return new Geometry(submodels);
        }
    }
}
