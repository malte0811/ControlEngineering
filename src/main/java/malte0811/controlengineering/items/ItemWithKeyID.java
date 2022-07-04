package malte0811.controlengineering.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemWithKeyID extends Item {
    public static final String LOCK_ID_KEY = "lockID";

    public ItemWithKeyID() {
        super(CEItems.simpleItemProperties().stacksTo(1));
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nullable Level level,
            @Nonnull List<Component> tooltipComponents,
            @Nonnull TooltipFlag isAdvanced
    ) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (isAdvanced.isAdvanced()) {
            tooltipComponents.add(Component.literal(getUUID(stack).toString()).withStyle(ChatFormatting.GRAY));
        }
    }

    public static UUID getUUID(ItemStack stack) {
        var tag = stack.getTag();
        if (tag != null && tag.contains(LOCK_ID_KEY)) {
            return stack.getTag().getUUID(LOCK_ID_KEY);
        } else {
            return addRandomId(stack);
        }
    }

    public static ItemStack create(RegistryObject<? extends ItemWithKeyID> item, UUID uuid) {
        final var result = item.get().getDefaultInstance();
        setUUID(result, uuid);
        return result;
    }

    public static void copyIdFrom(ItemStack to, ItemStack from) {
        setUUID(to, getUUID(from));
    }

    public static UUID addRandomId(ItemStack stack) {
        final var uuid = UUID.randomUUID();
        setUUID(stack, uuid);
        return uuid;
    }

    public static void setUUID(ItemStack stack, UUID id) {
        stack.getOrCreateTag().putUUID(LOCK_ID_KEY, id);
    }
}
