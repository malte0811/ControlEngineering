package malte0811.controlengineering.datagen;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonArray;
import malte0811.controlengineering.controlpanels.ComponentCostReloadListener;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.io.IOException;

public record ComponentCostGenerator(DataGenerator generator) implements DataProvider {
    @Override
    public void run(@Nonnull HashCache cache) throws IOException {
        addCosts(cache, PanelComponents.BUTTON,
                new IngredientWithSize(Tags.Items.DYES),
                new IngredientWithSize(Tags.Items.DUSTS_GLOWSTONE),
                new IngredientWithSize(Ingredient.of(Items.STONE_BUTTON))
        );
        addCosts(cache, PanelComponents.LABEL,
                new IngredientWithSize(Tags.Items.DYES_BLACK),
                //TODO tag?
                new IngredientWithSize(Ingredient.of(Items.PAPER))
        );
        addCosts(cache, PanelComponents.INDICATOR,
                new IngredientWithSize(Tags.Items.DYES),
                new IngredientWithSize(Tags.Items.DUSTS_GLOWSTONE)
        );
    }

    private void addCosts(
            HashCache cache, PanelComponentType<?, ?> component, IngredientWithSize... cost
    ) throws IOException {
        var result = new JsonArray();
        for (var ingredient : cost) {
            result.add(ingredient.serialize());
        }
        var componentName = component.getRegistryName();
        var outputPath = generator.getOutputFolder()
                .resolve("data")
                .resolve(componentName.getNamespace())
                .resolve(ComponentCostReloadListener.DIRECTORY)
                .resolve(componentName.getPath() + ".json");
        DataProvider.save(ComponentCostReloadListener.GSON, cache, result, outputPath);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Panel component costs";
    }
}
