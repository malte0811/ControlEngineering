package malte0811.controlengineering.datagen;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.BlueprintCraftingRecipeBuilder;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.datagen.recipes.KeyDataRecipeBuilder;
import malte0811.controlengineering.datagen.recipes.NoAdvancementShapedBuilder;
import malte0811.controlengineering.datagen.recipes.NoAdvancementShapelessBuilder;
import malte0811.controlengineering.datagen.recipes.SingleIngredRecipeBuilder;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.EmptyTapeItem;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.logic.clock.ClockTypes;
import malte0811.controlengineering.scope.module.ScopeModules;
import malte0811.controlengineering.util.RLUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

import static malte0811.controlengineering.loot.BlueprintChestModifier.SCOPE_COMPONENTS_BLUEPRINT;

public class Recipes extends RecipeProvider {
    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
        busRecipes(consumer);
        tapeRecipes(consumer);
        panelRecipes(consumer);
        clockRecipes(consumer);
        logicRecipes(consumer);
        scopeRecipes(consumer);
    }

    private void busRecipes(Consumer<FinishedRecipe> consumer) {
        NoAdvancementShapedBuilder.shaped(CEItems.BUS_WIRE_COIL)
                .pattern("pcp")
                .pattern("cpc")
                .pattern("pcp")
                .define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .define('c', IEItemRefs.REDSTONE_WIRE_COIL)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(CEBlocks.BUS_RELAY, 4)
                .pattern("prp")
                .pattern("bbb")
                .define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .define('b', Blocks.TERRACOTTA)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(CEBlocks.BUS_INTERFACE)
                .pattern("prp")
                .pattern("bcb")
                .define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .define('b', Blocks.TERRACOTTA)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('c', CEItems.BUS_WIRE_COIL)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(CEBlocks.LINE_ACCESS)
                .pattern("r b")
                .pattern("RcB")
                .define('r', IEItemRefs.REDSTONE_CONNECTOR)
                .define('R', IEItemRefs.REDSTONE_WIRE_COIL)
                .define('c', Items.COMPARATOR)
                .define('b', CEBlocks.BUS_RELAY)
                .define('B', CEItems.BUS_WIRE_COIL)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(CEBlocks.RS_REMAPPER)
                .pattern("r r")
                .pattern("ccc")
                .pattern("RbR")
                .define('r', IEItemRefs.REDSTONE_CONNECTOR)
                .define('R', IEItemRefs.REDSTONE_WIRE_COIL)
                .define('b', Items.COMPARATOR)
                .define('c', IETags.copperWire)
                .save(consumer);
    }

    private void tapeRecipes(Consumer<FinishedRecipe> consumer) {
        NoAdvancementShapedBuilder.shaped(CEBlocks.KEYPUNCH)
                .pattern("BCb")
                .pattern("pcp")
                .pattern("ppp")
                .define('c', CEBlocks.BUS_RELAY)
                .define('b', Items.STONE_BUTTON)
                .define('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
                .define('C', Items.CHAIN)
                .define('B', Items.IRON_BARS)
                .save(consumer);
        SingleIngredRecipeBuilder.special(CERecipeSerializers.GLUE_TAPE)
                .input(Ingredient.of(Tags.Items.SLIMEBALLS))
                .save(consumer, "glue_tape");
        NoAdvancementShapedBuilder.shaped(EmptyTapeItem.withLength(256))
                .pattern("ppp")
                .pattern("pdp")
                .pattern("ppp")
                .define('p', Items.PAPER)
                .define('d', Tags.Items.DYES_PINK)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(CEBlocks.SEQUENCER)
                .pattern("www")
                .pattern("cbr")
                .pattern("www")
                .define('c', CEBlocks.BUS_RELAY)
                .define('b', IEItemRefs.CIRCUIT_BOARD)
                .define('r', Items.REDSTONE)
                .define('w', IETags.getItemTag(IETags.treatedWood))
                .save(consumer);
    }

    private void panelRecipes(Consumer<FinishedRecipe> consumer) {
        SingleIngredRecipeBuilder.special(CERecipeSerializers.PANEL_RECIPE)
                .input(Ingredient.of(IETags.getTagsFor(EnumMetals.STEEL).plate))
                .save(consumer, "panel");
        NoAdvancementShapedBuilder.shaped(CEItems.PANEL_TOP)
                .pattern("ppp")
                .pattern("pwp")
                .define('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
                .define('w', IETags.copperWire)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(CEBlocks.PANEL_DESIGNER)
                .pattern("pge")
                .pattern("kww")
                .define('k', CEBlocks.KEYPUNCH)
                .define('w', IETags.getItemTag(IETags.treatedWood))
                .define('g', Tags.Items.DUSTS_GLOWSTONE)
                .define('e', Items.ENDER_EYE)
                .define('p', Blocks.PISTON)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(CEBlocks.PANEL_CNC)
                .pattern("sds")
                .pattern("wSw")
                .pattern("www")
                .define('s', IETags.steelRod)
                .define('d', IEItemRefs.DRILL_HEAD_IRON)
                .define('S', CEBlocks.SEQUENCER)
                .define('w', IETags.getItemTag(IETags.treatedWood))
                .save(consumer);
        KeyDataRecipeBuilder.shaped(CEItems.KEY, false)
                .pattern("haa")
                .pattern(" aa")
                .define('a', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .define('h', IETags.getItemTag(IETags.treatedWood))
                .save(consumer);
        KeyDataRecipeBuilder.shaped(CEItems.LOCK, true)
                .pattern(" cw")
                .pattern("haa")
                .pattern(" cw")
                .define('a', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .define('h', IETags.getItemTag(IETags.treatedWood))
                .define('w', IETags.copperWire)
                .define('c', IEItemRefs.COMPONENT_IRON)
                .save(consumer);
    }

    private void clockRecipes(Consumer<FinishedRecipe> consumer) {
        NoAdvancementShapedBuilder.shaped(ClockTypes.getItem(ClockTypes.ALWAYS_ON))
                .pattern("tpt")
                .pattern("rrr")
                .pattern("tpt")
                .define('t', Items.REDSTONE_TORCH)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .save(consumer);
        NoAdvancementShapelessBuilder.shapeless(ClockTypes.getItem(ClockTypes.WHILE_RS_ON))
                .requires(ClockTypes.getItem(ClockTypes.ALWAYS_ON))
                .requires(Tags.Items.DUSTS_REDSTONE)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(ClockTypes.getItem(ClockTypes.RISING_EDGE))
                .pattern("ppp")
                .pattern("dPr")
                .pattern("ppp")
                .define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .define('r', Items.REPEATER)
                .define('P', Items.PISTON)
                .define('d', Tags.Items.DUSTS_REDSTONE)
                .save(consumer);
    }

    private void logicRecipes(Consumer<FinishedRecipe> consumer) {
        NoAdvancementShapedBuilder.shaped(CEBlocks.LOGIC_CABINET)
                .pattern("aaa")
                .pattern("bLr")
                .pattern("aaa")
                .define('a', IETags.getItemTag(IETags.getTagsFor(EnumMetals.ALUMINUM).sheetmetal))
                .define('b', CEBlocks.BUS_RELAY)
                .define('L', IEItemRefs.LOGIC_UNIT)
                .define('r', IEItemRefs.RADIATOR)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(CEBlocks.LOGIC_WORKBENCH)
                .pattern("bsf")
                .pattern("c e")
                .define('s', IETags.getItemTag(IETags.treatedWoodSlab))
                .define('f', Items.FLINT_AND_STEEL)
                .define('c', IEItemRefs.CRATE)
                .define('b', IEItemRefs.BLUEPRINT)
                .define('e', IEItemRefs.LIGHT_ENGINEERING)
                .save(consumer);
        SpecialRecipeBuilder.special(CERecipeSerializers.SCHEMATIC_COPY.get())
                .save(consumer, ControlEngineering.MODID + ":schematic_copy");
        NoAdvancementShapelessBuilder.shapeless(CEItems.SCHEMATIC)
                .requires(Items.PAPER)
                .requires(Tags.Items.DYES_RED)
                .requires(Tags.Items.DYES_GREEN)
                .requires(IETags.hopGraphiteDust)
                .save(consumer);
    }

    private void scopeRecipes(Consumer<FinishedRecipe> consumer) {
        BlueprintCraftingRecipeBuilder.builder(SCOPE_COMPONENTS_BLUEPRINT, CEItems.CRT_TUBE.get())
                .addInput(Tags.Items.DUSTS_GLOWSTONE)
                .addInput(Tags.Items.DUSTS_REDSTONE)
                .addInput(Tags.Items.GLASS)
                .addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.COPPER).plate, 4))
                .addInput(IETags.getTagsFor(EnumMetals.NICKEL).plate)
                .build(consumer, RLUtils.ceLoc("crt_tube"));
        BlueprintCraftingRecipeBuilder.builder(SCOPE_COMPONENTS_BLUEPRINT, CEItems.SCOPE_MODULE_CASE.get())
                .addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.ALUMINUM).plate, 2))
                .addInput(IETags.copperWire)
                .addInput(IETags.plasticPlate)
                .build(consumer, RLUtils.ceLoc("scope_module_case"));
        NoAdvancementShapedBuilder.shaped(CEBlocks.SCOPE)
                .pattern("BlB")
                .pattern("Tac")
                .pattern("BBB")
                .define('l', Tags.Items.LEATHER)
                .define('B', IEItemRefs.LIGHT_BLUE_SHEETMETAL)
                .define('T', CEItems.CRT_TUBE)
                .define('a', IEItemRefs.COMPONENT_ADVANCED)
                .define('c', CEBlocks.BUS_RELAY)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(ScopeModules.ANALOG.item())
                .pattern("cCw")
                .pattern("wBM")
                .pattern("cCw")
                .define('c', Items.COMPARATOR)
                .define('C', IEItemRefs.COMPONENT_BASIC)
                .define('w', IETags.copperWire)
                .define('M', CEItems.SCOPE_MODULE_CASE)
                .define('B', IEItemRefs.CIRCUIT_BOARD)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(ScopeModules.DIGITAL.item())
                .pattern("ACM")
                .pattern("ABw")
                .pattern("ACM")
                .define('A', Items.REPEATER)
                .define('C', IEItemRefs.COMPONENT_BASIC)
                .define('B', IEItemRefs.CIRCUIT_BOARD)
                .define('M', CEItems.SCOPE_MODULE_CASE)
                .define('w', IETags.copperWire)
                .save(consumer);
        // TODO remove in 1.20? This is mostly so existing worlds can get access to the blueprint
        NoAdvancementShapedBuilder.shaped(BlueprintCraftingRecipe.getTypedBlueprint(SCOPE_COMPONENTS_BLUEPRINT))
                .pattern("cBa")
                .pattern("ddd")
                .pattern("ppp")
                .define('c', Items.COMPARATOR)
                .define('B', CEBlocks.BUS_RELAY)
                .define('a', Items.REPEATER)
                .define('d', Tags.Items.DYES_BLUE)
                .define('p', Items.PAPER)
                .save(consumer, RLUtils.ceLoc("scope_blueprint"));
    }
}
