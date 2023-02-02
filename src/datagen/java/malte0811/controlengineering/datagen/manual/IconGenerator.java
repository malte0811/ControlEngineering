package malte0811.controlengineering.datagen.manual;

import blusunrize.immersiveengineering.data.manual.icon.GameInitializationManager;
import blusunrize.immersiveengineering.data.manual.icon.ModelRenderer;
import malte0811.controlengineering.ControlEngineering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public record IconGenerator(
        PackOutput output, ExistingFileHelper helper, Path itemOutputDirectory
) implements DataProvider {
    @Override
    public CompletableFuture<?> run(CachedOutput p_236071_) {
        GameInitializationManager.getInstance().initialize(helper, output);
        try (ModelRenderer itemRenderer = new ModelRenderer(256, 256, itemOutputDirectory.toFile())) {
            ForgeRegistries.ITEMS.getEntries().forEach(entry -> {
                ResourceLocation name = entry.getKey().location();
                if (!ControlEngineering.MODID.equals(name.getNamespace())) { return; }
                Item item = entry.getValue();
                ModelResourceLocation modelLocation = new ModelResourceLocation(name, "inventory");
                ItemStack stackToRender = item.getDefaultInstance();

                final BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
                itemRenderer.renderModel(model, name.getNamespace() + "/" + name.getPath() + ".png", stackToRender);
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "Manual icon renderer";
    }
}
