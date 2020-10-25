package malte0811.controlengineering.items;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.util.ItemNBTUtil;
import malte0811.controlengineering.util.TextUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PunchedTapeItem extends Item {
    // Main tag
    private static final String BYTES_KEY = "bytes";
    // Share tag
    private static final String BYTE_COUNT_KEY = "byteCount";

    public PunchedTapeItem() {
        super(
                new Item.Properties()
                        .group(ControlEngineering.ITEM_GROUP)
                        .maxStackSize(1)
        );
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            items.add(setBytes(new ItemStack(this), "Test1".getBytes()));
            items.add(setBytes(new ItemStack(this), "Another test".getBytes()));
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
        byte[] data = getBytes(stack);
        TextUtil.addTooltipLine(
                tooltip,
                new TranslationTextComponent("controlengineering.tooltip.written_tape_bytes", data.length)
        );
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        CompoundNBT nbt = ItemNBTUtil.getTag(stack)
                .map(CompoundNBT::copy)
                .orElseGet(CompoundNBT::new);
        nbt.remove(BYTES_KEY);
        nbt.putInt(BYTE_COUNT_KEY, getBytes(stack).length);
        return nbt;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null) {
            setBytes(stack, new byte[nbt.getInt(BYTE_COUNT_KEY)]);
        }
    }

    public static byte[] getBytes(ItemStack tape) {
        return ItemNBTUtil.getTag(tape)
                .map(c -> c.getByteArray(BYTES_KEY))
                .orElseGet(() -> new byte[0]);
    }

    public static ItemStack setBytes(ItemStack tape, byte[] newData) {
        tape.getOrCreateTag().putByteArray(BYTES_KEY, newData);
        return tape;
    }
}
