package malte0811.controlengineering.items;

import malte0811.controlengineering.itemdata.CEDataComponents;
import malte0811.controlengineering.util.TextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.util.List;

public class EmptyTapeItem extends Item {
    public static final String EMPTY_TAPE_BYTES = "controlengineering.tooltip.empty_tape_bytes";

    public EmptyTapeItem() {
        super(new Item.Properties().stacksTo(1).component(CEDataComponents.EMPTY_TAPE_LENGTH, 0));
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nonnull TooltipContext context,
            @Nonnull List<Component> tooltip,
            @Nonnull TooltipFlag flagIn
    ) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        int length = getLength(stack);
        TextUtil.addTooltipLine(tooltip, Component.translatable(EMPTY_TAPE_BYTES, length));
    }

    public static ItemStack withLength(int length) {
        ItemStack result = new ItemStack(CEItems.EMPTY_TAPE.get());
        result.set(CEDataComponents.EMPTY_TAPE_LENGTH, length);
        return result;
    }

    public static int getLength(ItemStack tape) {
        return tape.getOrDefault(CEDataComponents.EMPTY_TAPE_LENGTH, 0);
    }
}
