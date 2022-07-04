package malte0811.controlengineering.client.model.logic;

import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blockentity.logic.LogicWorkbenchBlockEntity;
import malte0811.controlengineering.client.model.CEBakedModel;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry.ExpandedBlockModelDeserializer;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public record LogicWorkbenchModel(
        BakedModel workbench, BakedModel schematic, ModelState transforms
) implements CEBakedModel {
    private static final ModelProperty<Boolean> HAS_SCHEMATIC = new ModelProperty<>();

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull IModelData extraData
    ) {
        var quads = workbench.getQuads(state, side, rand, extraData);
        var hasSchematic = extraData.getData(HAS_SCHEMATIC);
        if (hasSchematic != Boolean.FALSE) {
            quads = new ArrayList<>(quads);
            quads.addAll(schematic.getQuads(state, side, rand, extraData));
        }
        return quads;
    }

    @Nonnull
    @Override
    public IModelData getModelData(
            @Nonnull BlockAndTintGetter level,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull IModelData modelData
    ) {
        if (level.getBlockEntity(pos) instanceof LogicWorkbenchBlockEntity workbench) {
            return CombinedModelData.combine(
                    modelData, new SinglePropertyModelData<>(workbench.getSchematic() != null, HAS_SCHEMATIC)
            );
        } else {
            return modelData;
        }
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull IModelData data) {
        return workbench.getParticleIcon(data);
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType transformType, PoseStack transform) {
        transforms.getPartTransformation(transformType).push(transform);
        return this;
    }

    public static class Loader implements IModelLoader<Geometry> {
        public static final String WORKBENCH = "workbench";
        public static final String SCHEMATIC = "schematic";

        @Nonnull
        @Override
        public Geometry read(
                @Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents
        ) {
            var modelGson = ExpandedBlockModelDeserializer.INSTANCE;
            var workbench = modelGson.fromJson(modelContents.get(WORKBENCH), BlockModel.class);
            var schematic = modelGson.fromJson(modelContents.get(SCHEMATIC), BlockModel.class);
            return new Geometry(workbench, schematic);
        }

        @Override
        public void onResourceManagerReload(@Nonnull ResourceManager pResourceManager) {}
    }

    private record Geometry(BlockModel workbench, BlockModel schematic) implements IModelGeometry<Geometry> {
        @Override
        public BakedModel bake(
                IModelConfiguration owner,
                ModelBakery bakery,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelTransform,
                ItemOverrides overrides,
                ResourceLocation modelLocation
        ) {
            return new LogicWorkbenchModel(
                    workbench.bake(bakery, workbench, spriteGetter, modelTransform, modelLocation, false),
                    schematic.bake(bakery, schematic, spriteGetter, modelTransform, modelLocation, false),
                    owner.getCombinedTransform()
            );
        }

        @Override
        public Collection<Material> getTextures(
                IModelConfiguration owner,
                Function<ResourceLocation, UnbakedModel> modelGetter,
                Set<Pair<String, String>> missingTextureErrors
        ) {
            var allTextures = new HashSet<>(workbench.getMaterials(modelGetter, missingTextureErrors));
            allTextures.addAll(schematic.getMaterials(modelGetter, missingTextureErrors));
            return allTextures;
        }
    }
}
