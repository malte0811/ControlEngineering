package malte0811.controlengineering.datagen;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonArray;
import malte0811.controlengineering.controlpanels.ComponentCostReloadListener;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.items.IEItemRefs;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public record ComponentCostGenerator(DataGenerator generator) implements DataProvider {
    @Override
    public void run(@Nonnull HashCache cache) throws IOException {
        var anyDye = new IngredientWithSize(Tags.Items.DYES);
        //TODO tag?
        var paper = new IngredientWithSize(Ingredient.of(Items.PAPER));
        var glowstone = new IngredientWithSize(Tags.Items.DUSTS_GLOWSTONE);
        var stoneButton = new IngredientWithSize(Ingredient.of(Items.STONE_BUTTON));
        var blackDye = new IngredientWithSize(Tags.Items.DYES_BLACK);
        var lever = new IngredientWithSize(Ingredient.of(Items.LEVER));
        var clock = new IngredientWithSize(Ingredient.of(Items.CLOCK));
        var coil = new IngredientWithSize(Ingredient.of(IEItemRefs.WIRE_COIL.get()));
        var graphite = new IngredientWithSize(IETags.hopGraphiteIngot);

        addCosts(cache, PanelComponents.BUTTON, anyDye, glowstone, stoneButton);
        addCosts(cache, PanelComponents.LABEL, blackDye, paper);
        addCosts(cache, PanelComponents.INDICATOR, anyDye, glowstone);
        addCosts(cache, PanelComponents.TOGGLE_SWITCH, lever);
        addCosts(cache, PanelComponents.COVERED_SWITCH, lever, paper, anyDye);
        addCosts(cache, PanelComponents.TIMED_BUTTON, anyDye, glowstone, stoneButton, clock);
        addCosts(cache, PanelComponents.PANEL_METER, paper, blackDye, coil);
        addCosts(cache, PanelComponents.VARIAC, blackDye, coil, graphite);
        for (var slider : List.of(PanelComponents.SLIDER_HOR, PanelComponents.SLIDER_VERT))
            addCosts(cache, slider, coil, glowstone, anyDye);
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
