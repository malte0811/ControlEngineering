package malte0811.controlengineering.crafting;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.util.CEDualCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public record GlueTapeRecipe(Ingredient glue) implements CraftingRecipe {
    public static final DualMapCodec<RegistryFriendlyByteBuf, GlueTapeRecipe> CODECS = CEDualCodecs.INGREDIENT
            .map(GlueTapeRecipe::new, GlueTapeRecipe::glue)
            .fieldOf("glue");

    @Override
    public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level worldIn) {
        return findMatch(inv) >= 0;
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull CraftingInput inv, HolderLookup.Provider access) {
        int match = findMatch(inv);
        if (match < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack tape1 = inv.getItem(match);
        ItemStack tape2 = inv.getItem(match + 2);
        ByteList first = PunchedTapeItem.getBytes(tape1);
        ByteList second = PunchedTapeItem.getBytes(tape2);
        ByteList combined = new ByteArrayList(first);
        combined.addAll(second);
        return PunchedTapeItem.withBytes(combined);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 1;
    }

    private int findMatch(CraftingInput inv) {
        for (int x = 0; x < inv.width() - 2; ++x) {
            for (int y = 0; y < inv.height(); ++y) {
                int offset = y * inv.width() + x;
                ItemStack tape1 = inv.getItem(offset);
                ItemStack glue = inv.getItem(offset + 1);
                ItemStack tape2 = inv.getItem(offset + 2);
                if (tape1.is(CEItems.PUNCHED_TAPE.get()) && this.glue.test(glue) && tape2.is(CEItems.PUNCHED_TAPE.get())) {
                    var totalLength = PunchedTapeItem.getBytes(tape1).size() + PunchedTapeItem.getBytes(tape2).size();
                    if (totalLength > KeypunchState.MAX_TAPE_LENGTH) {
                        return -1;
                    } else {
                        return offset;
                    }
                }
            }
        }
        return -1;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(HolderLookup.Provider access) {
        return CEItems.PUNCHED_TAPE.get().getDefaultInstance();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(
                Ingredient.EMPTY,
                Ingredient.of(CEItems.PUNCHED_TAPE.get()),
                glue,
                Ingredient.of(CEItems.PUNCHED_TAPE.get())
        );
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return CERecipeSerializers.GLUE_TAPE.get();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }
}
