package malte0811.controlengineering.datagen;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.crafting.noncrafting.ComponentCostRecipe;
import malte0811.controlengineering.items.IEItemRefs;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ComponentCostGenerator {

    public static void buildComponentCosts(@NotNull RecipeOutput out) {
        var anyDye = new IngredientWithSize(Tags.Items.DYES);
        //TODO tag?
        var paper = new IngredientWithSize(Ingredient.of(Items.PAPER));
        var glowstone = new IngredientWithSize(Tags.Items.DUSTS_GLOWSTONE);
        var stoneButton = new IngredientWithSize(Ingredient.of(Items.STONE_BUTTON));
        var blackDye = new IngredientWithSize(Tags.Items.DYES_BLACK);
        var lever = new IngredientWithSize(Ingredient.of(Items.LEVER));
        var clock = new IngredientWithSize(Ingredient.of(Items.CLOCK));
        var coil = new IngredientWithSize(Ingredient.of(IEItemRefs.COPPER_WIRE_COIL));
        var graphite = new IngredientWithSize(IETags.hopGraphiteIngot);

        addCosts(out, PanelComponents.BUTTON, anyDye, glowstone, stoneButton);
        addCosts(out, PanelComponents.LABEL, blackDye, paper);
        addCosts(out, PanelComponents.INDICATOR, anyDye, glowstone);
        addCosts(out, PanelComponents.TOGGLE_SWITCH, lever);
        addCosts(out, PanelComponents.COVERED_SWITCH, lever, paper, anyDye);
        addCosts(out, PanelComponents.TIMED_BUTTON, anyDye, glowstone, stoneButton, clock);
        addCosts(out, PanelComponents.PANEL_METER, paper, blackDye, coil);
        addCosts(out, PanelComponents.VARIAC, blackDye, coil, graphite);
        for (var slider : List.of(PanelComponents.SLIDER_HOR, PanelComponents.SLIDER_VERT))
            addCosts(out, slider, coil, glowstone, anyDye);
        addCosts(out, PanelComponents.KEY_SWITCH, new IngredientWithSize(IETags.copperWire));
    }

    private static void addCosts(
            RecipeOutput out, PanelComponentType<?, ?> component, IngredientWithSize... cost
    ) {
        out.accept(component.getCostLocation(), new ComponentCostRecipe(Arrays.asList(cost)), null);
    }
}
