package malte0811.controlengineering.datagen;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.noncrafting.ComponentCostRecipe;
import malte0811.controlengineering.items.IEItemRefs;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ComponentCostGenerator extends RecipeProvider {

    public ComponentCostGenerator(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void buildCraftingRecipes(@Nonnull Consumer<FinishedRecipe> out) {
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

    private void addCosts(
            Consumer<FinishedRecipe> out, PanelComponentType<?, ?> component, IngredientWithSize... cost
    ) {
        out.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(@Nonnull JsonObject fullJson) {
                var costJson = new JsonArray();
                for (var ingredient : cost) {
                    costJson.add(ingredient.serialize());
                }
                fullJson.add(ComponentCostRecipe.Serializer.COST_ARRAY_KEY, costJson);
            }

            @Nonnull
            @Override
            public ResourceLocation getId() {
                return component.getCostLocation();
            }

            @Nonnull
            @Override
            public RecipeSerializer<?> getType() {
                return CERecipeSerializers.COMPONENT_COST.get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }

    @Nonnull
    @Override
    public String getName() {
        return "Panel component costs";
    }
}
