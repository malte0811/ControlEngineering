package malte0811.controlengineering.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpecialModelLoader implements IModelLoader<SpecialModelLoader.SpecialModelGeometry> {
    private final Function<ItemCameraTransforms, ? extends IBakedModel> modelMaker;
    private final Collection<RenderMaterial> materials;

    public SpecialModelLoader(Supplier<? extends IBakedModel> modelMaker, ResourceLocation... materials) {
        this($ -> modelMaker.get(), materials);
    }

    public SpecialModelLoader(
            Function<ItemCameraTransforms, ? extends IBakedModel> modelMaker,
            ResourceLocation... materials
    ) {
        this.modelMaker = modelMaker;
        this.materials = Arrays.stream(materials)
                .map(rl -> new RenderMaterial(PlayerContainer.LOCATION_BLOCKS_TEXTURE, rl))
                .collect(Collectors.toList());
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {}

    @Nonnull
    @Override
    public SpecialModelGeometry read(
            @Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents
    ) {
        return new SpecialModelGeometry();
    }

    public class SpecialModelGeometry implements IModelGeometry<SpecialModelGeometry> {

        @Override
        public IBakedModel bake(
                IModelConfiguration owner,
                ModelBakery bakery,
                Function<RenderMaterial, TextureAtlasSprite> spriteGetter,
                IModelTransform modelTransform,
                ItemOverrideList overrides,
                ResourceLocation modelLocation
        ) {
            return modelMaker.apply(owner.getCameraTransforms());
        }

        @Override
        public Collection<RenderMaterial> getTextures(
                IModelConfiguration owner,
                Function<ResourceLocation, IUnbakedModel> modelGetter,
                Set<Pair<String, String>> missingTextureErrors
        ) {
            return materials;
        }
    }
}
