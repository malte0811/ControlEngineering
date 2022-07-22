package malte0811.controlengineering.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpecialModelLoader implements IGeometryLoader<SpecialModelLoader.SpecialModelGeometry> {
    private final BiFunction<ItemTransforms, ModelState, ? extends BakedModel> modelMaker;
    private final Collection<Material> materials;

    public SpecialModelLoader(
            BiFunction<ItemTransforms, ModelState, ? extends BakedModel> modelMaker,
            ResourceLocation... materials
    ) {
        this.modelMaker = modelMaker;
        this.materials = Arrays.stream(materials)
                .map(rl -> new Material(InventoryMenu.BLOCK_ATLAS, rl))
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public SpecialModelGeometry read(
            @Nonnull JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext
    ) {
        return new SpecialModelGeometry();
    }

    public class SpecialModelGeometry implements IUnbakedGeometry<SpecialModelGeometry> {

        @Override
        public BakedModel bake(
                IGeometryBakingContext owner,
                ModelBakery bakery,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelTransform,
                ItemOverrides overrides,
                ResourceLocation modelLocation
        ) {
            return modelMaker.apply(owner.getTransforms(), modelTransform);
        }

        @Override
        public Collection<Material> getMaterials(
                IGeometryBakingContext owner,
                Function<ResourceLocation, UnbakedModel> modelGetter,
                Set<Pair<String, String>> missingTextureErrors
        ) {
            return materials;
        }
    }
}
