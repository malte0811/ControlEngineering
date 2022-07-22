package malte0811.controlengineering.client.model.logic;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blockentity.logic.LogicWorkbenchBlockEntity;
import malte0811.controlengineering.client.model.CEBakedModel;
import malte0811.controlengineering.client.render.target.RenderUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public record LogicWorkbenchModel(
        BakedModel workbench, BakedModel schematic, ItemTransforms transforms
) implements CEBakedModel {
    private static final ModelProperty<Boolean> HAS_SCHEMATIC = new ModelProperty<>();

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            @Nonnull RandomSource rand,
            @Nonnull ModelData extraData,
            @Nullable RenderType layer
    ) {
        var quads = workbench.getQuads(state, side, rand, extraData, layer);
        var hasSchematic = extraData.get(HAS_SCHEMATIC);
        if (hasSchematic != Boolean.FALSE) {
            quads = new ArrayList<>(quads);
            quads.addAll(schematic.getQuads(state, side, rand, extraData, layer));
        }
        return quads;
    }

    @Nonnull
    @Override
    public ModelData getModelData(
            @Nonnull BlockAndTintGetter level,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull ModelData modelData
    ) {
        if (level.getBlockEntity(pos) instanceof LogicWorkbenchBlockEntity workbench) {
            return modelData.derive().with(HAS_SCHEMATIC, workbench.getSchematic() != null).build();
        } else {
            return modelData;
        }
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data) {
        return workbench.getParticleIcon(data);
    }

    @Nonnull
    @Override
    public BakedModel applyTransform(
            @Nonnull ItemTransforms.TransformType transformType,
            @Nonnull PoseStack poseStack,
            boolean applyLeftHandTransform
    ) {
        transforms.getTransform(transformType).apply(applyLeftHandTransform, poseStack);
        return this;
    }

    public static class Loader implements IGeometryLoader<Geometry> {
        public static final String WORKBENCH = "workbench";
        public static final String SCHEMATIC = "schematic";

        @Nonnull
        @Override
        public Geometry read(
                @Nonnull JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext
                ) {
            var modelGson = ExtendedBlockModelDeserializer.INSTANCE;
            var workbench = modelGson.fromJson(modelContents.get(WORKBENCH), BlockModel.class);
            var schematic = modelGson.fromJson(modelContents.get(SCHEMATIC), BlockModel.class);
            return new Geometry(workbench, schematic);
        }
    }

    private record Geometry(BlockModel workbench, BlockModel schematic) implements IUnbakedGeometry<Geometry> {
        @Override
        public BakedModel bake(
                IGeometryBakingContext owner,
                ModelBakery bakery,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelTransform,
                ItemOverrides overrides,
                ResourceLocation modelLocation
        ) {
            return new LogicWorkbenchModel(
                    workbench.bake(bakery, workbench, spriteGetter, modelTransform, modelLocation, false),
                    schematic.bake(bakery, schematic, spriteGetter, modelTransform, modelLocation, false),
                    owner.getTransforms()
            );
        }

        @Override
        public Collection<Material> getMaterials(
                IGeometryBakingContext owner,
                Function<ResourceLocation, UnbakedModel> modelGetter,
                Set<Pair<String, String>> missingTextureErrors
        ) {
            var allTextures = new HashSet<>(workbench.getMaterials(modelGetter, missingTextureErrors));
            allTextures.addAll(schematic.getMaterials(modelGetter, missingTextureErrors));
            return allTextures;
        }
    }
}
