package malte0811.controlengineering.items;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.util.ItemNBTUtil;
import malte0811.controlengineering.util.TextUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class EmptyTapeItem extends Item {
    private static final String LENGTH_KEY = "length";

    public EmptyTapeItem() {
        super(
                new Item.Properties()
                        .group(ControlEngineering.ITEM_GROUP)
                        .maxStackSize(1)
        );
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            items.add(setLength(new ItemStack(this), 16));
            items.add(setLength(new ItemStack(this), 256));
            items.add(setLength(new ItemStack(this), 8192));
        }
    }

    @Override
    public void addInformation(
            @Nonnull ItemStack stack,
            @Nullable World worldIn,
            @Nonnull List<ITextComponent> tooltip,
            @Nonnull ITooltipFlag flagIn
    ) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int length = getLength(stack);
        TextUtil.addTooltipLine(
                tooltip,
                new TranslationTextComponent("controlengineering.tooltip.empty_tape_bytes", length)
        );
    }

    public static int getLength(ItemStack tape) {
        return ItemNBTUtil.getTag(tape).map(c -> c.getInt(LENGTH_KEY)).orElse(0);
    }

    public static ItemStack setLength(ItemStack tape, int newLength) {
        tape.getOrCreateTag().putInt(LENGTH_KEY, newLength);
        return tape;
    }
}
