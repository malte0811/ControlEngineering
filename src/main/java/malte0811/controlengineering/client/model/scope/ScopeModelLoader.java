package malte0811.controlengineering.client.model.scope;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ScopeModelLoader implements IGeometryLoader<ScopeModelLoader.Unbaked> {
    public static final String MODULES_KEY = "modules";
    public static final String MAIN_KEY = "main";

    @Override
    public Unbaked read(JsonObject json, JsonDeserializationContext ctx) throws JsonParseException {
        final var moduleModelsJSON = json.getAsJsonObject(MODULES_KEY);
        Map<ResourceLocation, BlockModel> moduleModels = new HashMap<>();
        for (final var subModel : moduleModelsJSON.entrySet()) {
            final var key = new ResourceLocation(subModel.getKey());
            final BlockModel model = ctx.deserialize(subModel.getValue(), BlockModel.class);
            moduleModels.put(key, model);
        }
        final BlockModel mainModel = ctx.deserialize(json.get(MAIN_KEY), BlockModel.class);
        return new Unbaked(mainModel, moduleModels);
    }

    public record Unbaked(
            BlockModel mainModel, Map<ResourceLocation, BlockModel> moduleModels
    ) implements IUnbakedGeometry<Unbaked> {
        @Override
        public BakedModel bake(
                IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation
        ) {
            var rootTransform = context.getRootTransform();
            if (!rootTransform.isIdentity())
                modelState = new SimpleModelState(
                        modelState.getRotation().compose(rootTransform), modelState.isUvLocked()
                );
            Map<ResourceLocation, BakedModel> modules = new HashMap<>();
            for (final var entry : moduleModels.entrySet()) {
                final var baked = entry.getValue().bake(
                        baker, entry.getValue(), spriteGetter, modelState, modelLocation, true
                );
                modules.put(entry.getKey(), baked);
            }
            return new ScopeModel(
                    mainModel.bake(baker, mainModel, spriteGetter, modelState, modelLocation, true),
                    modules,
                    modelState.getRotation(),
                    context.getTransforms()
            );
        }
    }
}
