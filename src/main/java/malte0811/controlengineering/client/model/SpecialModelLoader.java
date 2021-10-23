package malte0811.controlengineering.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
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
    private final Function<ItemTransforms, ? extends BakedModel> modelMaker;
    private final Collection<Material> materials;

    public SpecialModelLoader(
            Function<ItemTransforms, ? extends BakedModel> modelMaker,
            ResourceLocation... materials
    ) {
        this.modelMaker = modelMaker;
        this.materials = Arrays.stream(materials)
                .map(rl -> new Material(InventoryMenu.BLOCK_ATLAS, rl))
                .collect(Collectors.toList());
    }

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {}

    @Nonnull
    @Override
    public SpecialModelGeometry read(
            @Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents
    ) {
        return new SpecialModelGeometry();
    }

    public class SpecialModelGeometry implements IModelGeometry<SpecialModelGeometry> {

        @Override
        public BakedModel bake(
                IModelConfiguration owner,
                ModelBakery bakery,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelTransform,
                ItemOverrides overrides,
                ResourceLocation modelLocation
        ) {
            return modelMaker.apply(owner.getCameraTransforms());
        }

        @Override
        public Collection<Material> getTextures(
                IModelConfiguration owner,
                Function<ResourceLocation, UnbakedModel> modelGetter,
                Set<Pair<String, String>> missingTextureErrors
        ) {
            return materials;
        }
    }
}
