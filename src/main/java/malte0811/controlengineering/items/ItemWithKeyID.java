package malte0811.controlengineering.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static malte0811.controlengineering.itemdata.CEDataComponents.LOCK_ID;

public class ItemWithKeyID extends Item {
    public ItemWithKeyID() {
        super(CEItems.simpleItemProperties().stacksTo(1).component(LOCK_ID, new UUID(0, 0)));
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nonnull TooltipContext context,
            @Nonnull List<Component> tooltipComponents,
            @Nonnull TooltipFlag isAdvanced
    ) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
        if (isAdvanced.isAdvanced()) {
            tooltipComponents.add(Component.literal(getUUID(stack).toString()).withStyle(ChatFormatting.GRAY));
        }
    }

    public static UUID getUUID(ItemStack stack) {
        if (!stack.has(LOCK_ID)) {
            addRandomId(stack);
        }
        return stack.get(LOCK_ID);
    }

    public static ItemStack create(Supplier<? extends ItemWithKeyID> item, UUID uuid) {
        final var result = item.get().getDefaultInstance();
        result.set(LOCK_ID, uuid);
        return result;
    }

    public static void copyIdFrom(ItemStack to, ItemStack from) {
        to.set(LOCK_ID, getUUID(from));
    }

    public static UUID addRandomId(ItemStack stack) {
        final var uuid = UUID.randomUUID();
        stack.set(LOCK_ID, uuid);
        return uuid;
    }
}
