package malte0811.controlengineering.logic.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class DynamicLogicModelLoader implements IModelLoader<DynamicLogicModelLoader.DynamicLogicGeometry> {
    public static final String BOARD_KEY = "board";
    public static final String TUBE_KEY = "tube";

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {}

    @Nonnull
    @Override
    public DynamicLogicGeometry read(
            @Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents
    ) {
        IUnbakedModel boardModel = ModelLoader.defaultModelGetter().apply(
                new ResourceLocation(modelContents.get(BOARD_KEY).getAsString())
        );
        IUnbakedModel tubeModel = ModelLoader.defaultModelGetter().apply(
                new ResourceLocation(modelContents.get(TUBE_KEY).getAsString())
        );
        return new DynamicLogicGeometry(boardModel, tubeModel);
    }

    public static class DynamicLogicGeometry implements IModelGeometry<DynamicLogicGeometry> {
        private final IUnbakedModel board;
        private final IUnbakedModel tube;

        public DynamicLogicGeometry(IUnbakedModel board, IUnbakedModel tube) {
            this.board = board;
            this.tube = tube;
        }

        @Override
        public IBakedModel bake(
                IModelConfiguration owner,
                ModelBakery bakery,
                Function<RenderMaterial, TextureAtlasSprite> spriteGetter,
                IModelTransform modelTransform,
                ItemOverrideList overrides,
                ResourceLocation modelLocation
        ) {
            return new DynamicLogicModel(board, tube, bakery, spriteGetter, modelTransform);
        }

        @Override
        public Collection<RenderMaterial> getTextures(
                IModelConfiguration owner,
                Function<ResourceLocation, IUnbakedModel> modelGetter,
                Set<Pair<String, String>> missingTextureErrors
        ) {
            Set<RenderMaterial> textures = new HashSet<>();
            textures.addAll(board.getTextures(modelGetter, missingTextureErrors));
            textures.addAll(tube.getTextures(modelGetter, missingTextureErrors));
            return textures;
        }
    }
}
