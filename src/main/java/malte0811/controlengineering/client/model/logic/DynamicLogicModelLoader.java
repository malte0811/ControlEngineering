package malte0811.controlengineering.client.model.logic;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class DynamicLogicModelLoader implements IGeometryLoader<DynamicLogicModelLoader.DynamicLogicGeometry> {
    public static final String BOARD_KEY = "board";
    public static final String TUBE_KEY = "tube";

    @Nonnull
    @Override
    public DynamicLogicGeometry read(
            @Nonnull JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext
    ) {
        var bakery = Minecraft.getInstance().getModelManager().getModelBakery();
        UnbakedModel boardModel = bakery.getModel(new ResourceLocation(modelContents.get(BOARD_KEY).getAsString()));
        UnbakedModel tubeModel = bakery.getModel(new ResourceLocation(modelContents.get(TUBE_KEY).getAsString()));
        return new DynamicLogicGeometry(boardModel, tubeModel);
    }

    public record DynamicLogicGeometry(
            UnbakedModel board, UnbakedModel tube
    ) implements IUnbakedGeometry<DynamicLogicGeometry> {

        @Override
        public BakedModel bake(
                IGeometryBakingContext owner,
                ModelBakery bakery,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelTransform,
                ItemOverrides overrides,
                ResourceLocation modelLocation
        ) {
            return new DynamicLogicModel(board, tube, bakery, spriteGetter, modelTransform);
        }

        @Override
        public Collection<Material> getMaterials(
                IGeometryBakingContext owner,
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
