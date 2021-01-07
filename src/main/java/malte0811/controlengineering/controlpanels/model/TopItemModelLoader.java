package malte0811.controlengineering.controlpanels.model;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.render.PanelRenderer;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class TopItemModelLoader implements IModelLoader<TopItemModelLoader.UnbakedModel> {
    public static final ResourceLocation ID = new ResourceLocation(ControlEngineering.MODID, "panel_top");

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {}

    @Nonnull
    @Override
    public UnbakedModel read(
            @Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents
    ) {
        return new UnbakedModel();
    }

    public static class UnbakedModel implements IModelGeometry<UnbakedModel> {

        @Override
        public Collection<RenderMaterial> getTextures(
                IModelConfiguration owner,
                Function<ResourceLocation, IUnbakedModel> modelGetter,
                Set<Pair<String, String>> missingTextureErrors
        ) {
            return ImmutableList.of(new RenderMaterial(
                    PlayerContainer.LOCATION_BLOCKS_TEXTURE, PanelRenderer.PANEL_TEXTURE_LOC
            ));
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
            return new PanelTopItemModel();
        }
    }
}
