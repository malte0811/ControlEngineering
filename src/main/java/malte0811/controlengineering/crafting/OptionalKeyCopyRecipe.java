package malte0811.controlengineering.crafting;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.ItemWithKeyID;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class OptionalKeyCopyRecipe extends ShapedRecipe {
    private final boolean isIdOptional;
    private final NonNullList<Ingredient> ingredientsWithIdSource;

    public OptionalKeyCopyRecipe(ShapedRecipe baseRecipe, boolean isIdOptional) {
        super(
                baseRecipe.getId(), baseRecipe.getGroup(),
                baseRecipe.getWidth(), baseRecipe.getHeight(),
                baseRecipe.getIngredients(), baseRecipe.getResultItem()
        );
        Preconditions.checkArgument(getResultItem().getItem() instanceof ItemWithKeyID);
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
    public boolean matches(@Nonnull CraftingContainer inv, @Nonnull Level level) {
        final var idSource = removeIDSource(inv);
        if (!isIdOptional && idSource == null) {
            return false;
        }
        return super.matches(idSource != null ? idSource.withoutSource : inv, level);
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull CraftingContainer inv) {
        final var match = removeIDSource(inv);
        final ItemStack producedItem;
        if (match != null) {
            producedItem = super.assemble(match.withoutSource);
            ItemWithKeyID.copyIdFrom(producedItem, inv.getItem(match.slotId));
        } else {
            producedItem = super.assemble(inv);
            ItemWithKeyID.addRandomId(producedItem);
        }
        return producedItem;
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingContainer container) {
        final var idSource = removeIDSource(container);
        final var remaining = super.getRemainingItems(container);
        if (idSource != null) {
            remaining.set(idSource.slotId, container.getItem(idSource.slotId).copy());
        }
        return remaining;
    }

    @Nullable
    private Match removeIDSource(@Nonnull CraftingContainer inv) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (inv.getItem(i).getItem() instanceof ItemWithKeyID) {
                CraftingContainer newContainer = new CraftingContainer(
                        new AbstractContainerMenu(null, 0) {
                            @Override
                            public boolean stillValid(@Nonnull Player player) {
                                return false;
                            }
                        }, inv.getWidth(), inv.getHeight()
                );
                for (int j = 0; j < inv.getContainerSize(); ++j) {
                    if (j != i) {
                        newContainer.setItem(j, inv.getItem(j));
                    }
                }
                return new Match(i, newContainer);
            }
        }
        return null;
    }

    public boolean isIdOptional() {
        return isIdOptional;
    }

    private record Match(int slotId, CraftingContainer withoutSource) {}
}
