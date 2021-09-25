package malte0811.controlengineering.logic.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
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
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {}

    @Nonnull
    @Override
    public DynamicLogicGeometry read(
            @Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents
    ) {
        UnbakedModel boardModel = ModelLoader.defaultModelGetter().apply(
                new ResourceLocation(modelContents.get(BOARD_KEY).getAsString())
        );
        UnbakedModel tubeModel = ModelLoader.defaultModelGetter().apply(
                new ResourceLocation(modelContents.get(TUBE_KEY).getAsString())
        );
        return new DynamicLogicGeometry(boardModel, tubeModel);
    }

    public static class DynamicLogicGeometry implements IModelGeometry<DynamicLogicGeometry> {
        private final UnbakedModel board;
        private final UnbakedModel tube;

        public DynamicLogicGeometry(UnbakedModel board, UnbakedModel tube) {
            this.board = board;
            this.tube = tube;
        }

        @Override
        public BakedModel bake(
                IModelConfiguration owner,
                ModelBakery bakery,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelTransform,
                ItemOverrides overrides,
                ResourceLocation modelLocation
        ) {
            return new DynamicLogicModel(board, tube, bakery, spriteGetter, modelTransform);
        }

        @Override
        public Collection<Material> getTextures(
                IModelConfiguration owner,
                Function<ResourceLocation, UnbakedModel> modelGetter,
                Set<Pair<String, String>> missingTextureErrors
        ) {
            Set<Material> textures = new HashSet<>();
            textures.addAll(board.getMaterials(modelGetter, missingTextureErrors));
            textures.addAll(tube.getMaterials(modelGetter, missingTextureErrors));
            return textures;
        }
    }
}
