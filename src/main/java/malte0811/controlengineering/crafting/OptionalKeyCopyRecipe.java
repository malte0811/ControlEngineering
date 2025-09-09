package malte0811.controlengineering.crafting;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.ItemWithKeyID;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionalKeyCopyRecipe extends ShapedRecipe {
    private static final DualMapCodec<RegistryFriendlyByteBuf, ShapedRecipe> BASE_CODEC = new DualMapCodec<>(
            ShapedRecipe.Serializer.CODEC, ShapedRecipe.Serializer.STREAM_CODEC
    );
    public static final DualMapCodec<RegistryFriendlyByteBuf, OptionalKeyCopyRecipe> CODECS = DualCompositeMapCodecs.composite(
            BASE_CODEC, r -> r,
            DualCodecs.BOOL.fieldOf("isIdOptional"), OptionalKeyCopyRecipe::isIdOptional,
            OptionalKeyCopyRecipe::new
    );

    private final boolean isIdOptional;
    private final NonNullList<Ingredient> ingredientsWithIdSource;

    public OptionalKeyCopyRecipe(ShapedRecipe baseRecipe, boolean isIdOptional) {
        super(
                baseRecipe.getGroup(), CraftingBookCategory.MISC,
                baseRecipe.getWidth(), baseRecipe.getHeight(),
                baseRecipe.getIngredients(), baseRecipe.getResultItem(null)
        );
        Preconditions.checkArgument(getResultItem(null).getItem() instanceof ItemWithKeyID);
        this.isIdOptional = isIdOptional;
        this.ingredientsWithIdSource = NonNullList.create();
        this.ingredientsWithIdSource.addAll(baseRecipe.getIngredients());
        for (int i = 0; i < this.ingredientsWithIdSource.size(); ++i) {
            if (this.ingredientsWithIdSource.get(i).isEmpty()) {
                Item[] allowedItems;
                if (isIdOptional) {
                    allowedItems = new Item[]{Items.AIR, CEItems.KEY.get(), CEItems.LOCK.get()};
                } else {
                    allowedItems = new Item[]{CEItems.KEY.get(), CEItems.LOCK.get()};
                }
                // Specify values directly, otherwise empty stacks are filtered out
                this.ingredientsWithIdSource.set(i, Ingredient.fromValues(
                        Arrays.stream(allowedItems).map(Item::getDefaultInstance).map(Ingredient.ItemValue::new)
                ));
                break;
            }
        }
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredientsWithIdSource;
    }

    @Override
    public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level level) {
        final var idSource = removeIDSource(inv);
        if (!isIdOptional && idSource == null) {
            return false;
        }
        return super.matches(idSource != null ? idSource.withoutSource : inv, level);
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull CraftingInput inv, HolderLookup.Provider access) {
        final var match = removeIDSource(inv);
        final ItemStack producedItem;
        if (match != null) {
            producedItem = super.assemble(match.withoutSource, access);
            ItemWithKeyID.copyIdFrom(producedItem, inv.getItem(match.slotId));
        } else {
            producedItem = super.assemble(inv, access);
            ItemWithKeyID.addRandomId(producedItem);
        }
        return producedItem;
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingInput container) {
        final var idSource = removeIDSource(container);
        final var remaining = super.getRemainingItems(container);
        if (idSource != null) {
            remaining.set(idSource.slotId, container.getItem(idSource.slotId).copy());
        }
        return remaining;
    }

    @Nullable
    private Match removeIDSource(@Nonnull CraftingInput inv) {
        for (int i = 0; i < inv.size(); ++i) {
            if (inv.getItem(i).getItem() instanceof ItemWithKeyID) {
                List<ItemStack> modified = new ArrayList<>();
                for (int j = 0; j < inv.size(); ++j) {
                    modified.add(i != j ? inv.getItem(i) : ItemStack.EMPTY);
                }
                CraftingInput newContainer = CraftingInput.of(inv.width(), inv.height(), modified);
                return new Match(i, newContainer);
            }
        }
        return null;
    }

    public boolean isIdOptional() {
        return isIdOptional;
    }

    private record Match(int slotId, CraftingInput withoutSource) { }
}
