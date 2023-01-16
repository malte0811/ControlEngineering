package malte0811.controlengineering.client.model.logic;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class DynamicLogicModelLoader implements IGeometryLoader<DynamicLogicModelLoader.DynamicLogicGeometry> {
    public static final String BOARD_KEY = "board";
    public static final String TUBE_KEY = "tube";

    @Nonnull
    @Override
    public DynamicLogicGeometry read(
            @Nonnull JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext
    ) {
        return new DynamicLogicGeometry(getResLoc(modelContents, BOARD_KEY), getResLoc(modelContents, TUBE_KEY));
    }

    private static ResourceLocation getResLoc(JsonObject obj, String key) {
        return new ResourceLocation(obj.get(key).getAsString());
    }

    public record DynamicLogicGeometry(
            ResourceLocation board, ResourceLocation tube
    ) implements IUnbakedGeometry<DynamicLogicGeometry> {

        @Override
        public BakedModel bake(
                IGeometryBakingContext context,
                ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelState,
                ItemOverrides overrides,
                ResourceLocation modelLocation
        ) {
            return new DynamicLogicModel(board, tube, baker, spriteGetter, modelState);
        }
    }
}
