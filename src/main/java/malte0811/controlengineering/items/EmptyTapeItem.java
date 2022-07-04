package malte0811.controlengineering.items;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.util.ItemNBTUtil;
import malte0811.controlengineering.util.TextUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class EmptyTapeItem extends Item {
    private static final String LENGTH_KEY = "length";
    public static final String EMPTY_TAPE_BYTES = "controlengineering.tooltip.empty_tape_bytes";

    public EmptyTapeItem() {
        super(new Item.Properties().tab(ControlEngineering.ITEM_GROUP).stacksTo(1));
    }

    @Override
    public void fillItemCategory(@Nonnull CreativeModeTab group, @Nonnull NonNullList<ItemStack> items) {
        if (allowedIn(group)) {
            items.add(withLength(16));
            items.add(withLength(256));
        }
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nullable Level worldIn,
            @Nonnull List<Component> tooltip,
            @Nonnull TooltipFlag flagIn
    ) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        int length = getLength(stack);
        TextUtil.addTooltipLine(tooltip, Component.translatable(EMPTY_TAPE_BYTES, length));
    }

    public static ItemStack withLength(int length) {
        ItemStack result = new ItemStack(CEItems.EMPTY_TAPE.get());
        setLength(result, length);
        return result;
    }

    public static int getLength(ItemStack tape) {
        if (tape.getItem() != CEItems.EMPTY_TAPE.get()) {
            return 0;
        } else {
            return ItemNBTUtil.getTag(tape).map(c -> c.getInt(LENGTH_KEY)).orElse(0);
        }
    }

    public static ItemStack setLength(ItemStack tape, int newLength) {
        tape.getOrCreateTag().putInt(LENGTH_KEY, newLength);
        return tape;
    }
}
