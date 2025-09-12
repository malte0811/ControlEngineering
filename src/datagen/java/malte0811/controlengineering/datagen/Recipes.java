package malte0811.controlengineering.datagen;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.data.recipes.builder.BlueprintCraftingRecipeBuilder;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.crafting.GlueTapeRecipe;
import malte0811.controlengineering.crafting.OptionalKeyCopyRecipe;
import malte0811.controlengineering.crafting.PanelRecipe;
import malte0811.controlengineering.crafting.SchematicCopyRecipe;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.EmptyTapeItem;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.logic.clock.ClockTypes;
import malte0811.controlengineering.scope.module.ScopeModules;
import malte0811.controlengineering.util.RLUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static malte0811.controlengineering.loot.BlueprintChestModifier.SCOPE_COMPONENTS_BLUEPRINT;

public class Recipes extends RecipeProvider {
    private final ExistingFileHelper existingFileHelper;

    public Recipes(
            PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper
    ) {
        super(output, provider);
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    protected void buildRecipes(RecipeOutput output, HolderLookup.Provider holderLookup) {
        busRecipes(output);
        tapeRecipes(output);
        panelRecipes(output);
        clockRecipes(output);
        logicRecipes(output);
        scopeRecipes(output);
        try {
            ServerFontData.buildServerFontData(output, existingFileHelper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ComponentCostGenerator.buildComponentCosts(output);
    }

    private void busRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEItems.BUS_WIRE_COIL)
                .pattern("pcp")
                .pattern("cpc")
                .pattern("pcp")
                .define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .define('c', IEItemRefs.REDSTONE_WIRE_COIL)
                        .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEBlocks.BUS_RELAY, 4)
                .pattern("prp")
                .pattern("bbb")
                .define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .define('b', Blocks.TERRACOTTA)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEBlocks.BUS_INTERFACE)
                .pattern("prp")
                .pattern("bcb")
                .define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .define('b', Blocks.TERRACOTTA)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('c', CEItems.BUS_WIRE_COIL)
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEBlocks.LINE_ACCESS)
                .pattern("r b")
                .pattern("RcB")
                .define('r', IEItemRefs.REDSTONE_CONNECTOR)
                .define('R', IEItemRefs.REDSTONE_WIRE_COIL)
                .define('c', Items.COMPARATOR)
                .define('b', CEBlocks.BUS_RELAY)
                .define('B', CEItems.BUS_WIRE_COIL)
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEBlocks.RS_REMAPPER)
                .pattern("r r")
                .pattern("ccc")
                .pattern("RbR")
                .define('r', IEItemRefs.REDSTONE_CONNECTOR)
                .define('R', IEItemRefs.REDSTONE_WIRE_COIL)
                .define('b', Items.COMPARATOR)
                .define('c', IETags.copperWire)
                .save(output);
    }

    private void tapeRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEBlocks.KEYPUNCH)
                .pattern("BCb")
                .pattern("pcp")
                .pattern("ppp")
                .define('c', CEBlocks.BUS_RELAY)
                .define('b', Items.STONE_BUTTON)
                .define('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
                .define('C', Items.CHAIN)
                .define('B', Items.IRON_BARS)
                .save(output);
        output.accept(RLUtils.ceLoc("glue_tape"), new GlueTapeRecipe(Ingredient.of(Tags.Items.SLIMEBALLS)), null);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, EmptyTapeItem.withLength(256))
                .pattern("ppp")
                .pattern("pdp")
                .pattern("ppp")
                .define('p', Items.PAPER)
                .define('d', Tags.Items.DYES_PINK)
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEBlocks.SEQUENCER)
                .pattern("www")
                .pattern("cbr")
                .pattern("www")
                .define('c', CEBlocks.BUS_RELAY)
                .define('b', IEItemRefs.CIRCUIT_BOARD)
                .define('r', Items.REDSTONE)
                .define('w', IETags.getItemTag(IETags.treatedWood))
                .save(output);
    }

    private void panelRecipes(RecipeOutput output) {
        output.accept(
                RLUtils.ceLoc("panel"),
                new PanelRecipe(Ingredient.of(IETags.getTagsFor(EnumMetals.STEEL).plate)),
                null
        );
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEItems.PANEL_TOP)
                .pattern("ppp")
                .pattern("pwp")
                .define('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
                .define('w', IETags.copperWire)
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEBlocks.PANEL_DESIGNER)
                .pattern("pge")
                .pattern("kww")
                .define('k', CEBlocks.KEYPUNCH)
                .define('w', IETags.getItemTag(IETags.treatedWood))
                .define('g', Tags.Items.DUSTS_GLOWSTONE)
                .define('e', Items.ENDER_EYE)
                .define('p', Blocks.PISTON)
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEBlocks.PANEL_CNC)
                .pattern("sds")
                .pattern("wSw")
                .pattern("www")
                .save(output);
        var keyBaseRecipe = new ShapedRecipe("misc", CraftingBookCategory.MISC, ShapedRecipePattern.of(
                Map.of(
                        'a', Ingredient.of(IETags.getTagsFor(EnumMetals.ALUMINUM).plate),
                        'h', Ingredient.of(IETags.getItemTag(IETags.treatedWood))
                ), "haa", " aa"), CEItems.KEY.toStack());
        output.accept(RLUtils.ceLoc("key"), new OptionalKeyCopyRecipe(keyBaseRecipe, false), null);
        var lockBaseRecipe = new ShapedRecipe("misc", CraftingBookCategory.MISC, ShapedRecipePattern.of(
                Map.of(
                        'a', Ingredient.of(IETags.getTagsFor(EnumMetals.ALUMINUM).plate),
                        'h', Ingredient.of(IETags.getItemTag(IETags.treatedWood)),
                        'w', Ingredient.of(IETags.copperWire),
                        'c', Ingredient.of(IEItemRefs.COMPONENT_IRON)
                ), " cw", "haa", " cw"), CEItems.LOCK.toStack());
        output.accept(RLUtils.ceLoc("lock"), new OptionalKeyCopyRecipe(lockBaseRecipe, true), null);
    }

    private void clockRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ClockTypes.getItem(ClockTypes.ALWAYS_ON))
                .pattern("tpt")
                .pattern("rrr")
                .pattern("tpt")
                .define('t', Items.REDSTONE_TORCH)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ClockTypes.getItem(ClockTypes.WHILE_RS_ON))
                .requires(ClockTypes.getItem(ClockTypes.ALWAYS_ON))
                .requires(Tags.Items.DUSTS_REDSTONE)
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ClockTypes.getItem(ClockTypes.RISING_EDGE))
                .pattern("ppp")
                .pattern("dPr")
                .pattern("ppp")
                .define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .define('r', Items.REPEATER)
                .define('P', Items.PISTON)
                .define('d', Tags.Items.DUSTS_REDSTONE)
                .save(output);
    }

    private void logicRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEBlocks.LOGIC_CABINET)
                .pattern("aaa")
                .pattern("bLr")
                .pattern("aaa")
                .define('a', IETags.getItemTag(IETags.getTagsFor(EnumMetals.ALUMINUM).sheetmetal))
                .define('b', CEBlocks.BUS_RELAY)
                .define('L', IEItemRefs.LOGIC_UNIT.get())
                .define('r', IEItemRefs.RADIATOR.get())
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEBlocks.LOGIC_WORKBENCH)
                .pattern("bsf")
                .pattern("c e")
                .define('s', IETags.getItemTag(IETags.treatedWoodSlab))
                .define('f', Items.FLINT_AND_STEEL)
                .define('c', IEItemRefs.CRATE.get())
                .define('b', IEItemRefs.BLUEPRINT)
                .define('e', IEItemRefs.LIGHT_ENGINEERING.get())
                .save(output);
        output.accept(RLUtils.ceLoc("schematic_copy"), new SchematicCopyRecipe(), null);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CEItems.SCHEMATIC)
                .requires(Items.PAPER)
                .requires(Tags.Items.DYES_RED)
                .requires(Tags.Items.DYES_GREEN)
                .requires(IETags.hopGraphiteDust)
                .save(output);
    }

    private void scopeRecipes(RecipeOutput output) {
        BlueprintCraftingRecipeBuilder.builder()
                .input(Tags.Items.DUSTS_GLOWSTONE)
                .input(Tags.Items.DUSTS_REDSTONE)
                .input(Tags.Items.GLASS_BLOCKS)
                .input(new IngredientWithSize(IETags.getTagsFor(EnumMetals.COPPER).plate, 4))
                .input(IETags.getTagsFor(EnumMetals.NICKEL).plate)
                .category(SCOPE_COMPONENTS_BLUEPRINT)
                .output(CEItems.CRT_TUBE)
                .build(output, RLUtils.ceLoc("crt_tube"));
        BlueprintCraftingRecipeBuilder.builder()
                .input(new IngredientWithSize(IETags.getTagsFor(EnumMetals.ALUMINUM).plate, 2))
                .input(IETags.copperWire)
                .input(IETags.plasticPlate)
                .category(SCOPE_COMPONENTS_BLUEPRINT)
                .output(CEItems.SCOPE_MODULE_CASE)
                .build(output, RLUtils.ceLoc("scope_module_case"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CEBlocks.SCOPE)
                .pattern("BlB")
                .pattern("Tac")
                .pattern("BBB")
                .define('l', Tags.Items.LEATHERS)
                .define('B', IEItemRefs.LIGHT_BLUE_SHEETMETAL.get())
                .define('T', CEItems.CRT_TUBE)
                .define('a', IEItemRefs.COMPONENT_ADVANCED)
                .define('c', CEBlocks.BUS_RELAY)
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ScopeModules.ANALOG.item())
                .pattern("cCw")
                .pattern("wBM")
                .pattern("cCw")
                .define('c', Items.COMPARATOR)
                .define('C', IEItemRefs.COMPONENT_BASIC)
                .define('w', IETags.copperWire)
                .define('M', CEItems.SCOPE_MODULE_CASE)
                .define('B', IEItemRefs.CIRCUIT_BOARD)
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ScopeModules.DIGITAL.item())
                .pattern("ACM")
                .pattern("ABw")
                .pattern("ACM")
                .define('A', Items.REPEATER)
                .define('C', IEItemRefs.COMPONENT_BASIC)
                .define('B', IEItemRefs.CIRCUIT_BOARD)
                .define('M', CEItems.SCOPE_MODULE_CASE)
                .define('w', IETags.copperWire)
                .save(output);
        // TODO remove in 1.20? This is mostly so existing worlds can get access to the blueprint
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlueprintCraftingRecipe.getTypedBlueprint(SCOPE_COMPONENTS_BLUEPRINT))
                .pattern("cBa")
                .pattern("ddd")
                .pattern("ppp")
                .define('c', Items.COMPARATOR)
                .define('B', CEBlocks.BUS_RELAY)
                .define('a', Items.REPEATER)
                .define('d', Tags.Items.DYES_BLUE)
                .define('p', Items.PAPER)
                .save(output, RLUtils.ceLoc("scope_blueprint"));
    }
}
