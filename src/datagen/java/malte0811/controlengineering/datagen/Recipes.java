package malte0811.controlengineering.datagen;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.datagen.recipes.NoAdvancementShapedBuilder;
import malte0811.controlengineering.datagen.recipes.NoAdvancementShapelessBuilder;
import malte0811.controlengineering.datagen.recipes.SingleIngredRecipeBuilder;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.EmptyTapeItem;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.logic.clock.ClockTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

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
    }

    private void busRecipes(Consumer<FinishedRecipe> consumer) {
        NoAdvancementShapedBuilder.shaped(CEItems.BUS_WIRE_COIL)
                .pattern("pcp")
                .pattern("cpc")
                .pattern("pcp")
                .define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
                .define('c', IEItemRefs.REDSTONE_WIRE_COIL)
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(CEBlocks.BUS_RELAY.get(), 4)
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
                .define('c', CEItems.BUS_WIRE_COIL.get())
                .save(consumer);
        NoAdvancementShapedBuilder.shaped(CEBlocks.LINE_ACCESS)
                .pattern("r b")
                .pattern("RcB")
                .define('r', IEItemRefs.REDSTONE_CONNECTOR)
                .define('R', IEItemRefs.REDSTONE_WIRE_COIL)
                .define('c', Items.COMPARATOR)
                .define('b', CEBlocks.BUS_RELAY.get())
                .define('B', CEItems.BUS_WIRE_COIL.get())
                .save(consumer);
    }

    private void tapeRecipes(Consumer<FinishedRecipe> consumer) {
        NoAdvancementShapedBuilder.shaped(CEBlocks.KEYPUNCH)
                .pattern("BCb")
                .pattern("pcp")
                .pattern("ppp")
                .define('c', CEBlocks.BUS_RELAY.get())
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
        NoAdvancementShapedBuilder.shaped(CEBlocks.SEQUENCER.get())
                .pattern("www")
                .pattern("cbr")
                .pattern("www")
                .define('c', CEBlocks.BUS_RELAY.get())
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
                .define('k', CEBlocks.KEYPUNCH.get())
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
                .define('S', CEBlocks.SEQUENCER.get())
                .define('w', IETags.getItemTag(IETags.treatedWood))
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
}
