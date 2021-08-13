package malte0811.controlengineering.items;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.tape.ViewTapeScreen;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.Constants;
import malte0811.controlengineering.util.ItemNBTUtil;
import malte0811.controlengineering.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
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

    public PunchedTapeItem() {
        super(
                new Item.Properties()
                        .group(ControlEngineering.ITEM_GROUP)
                        .maxStackSize(1)
        );
    }

    public static ItemStack withBytes(byte[] bytes) {
        ItemStack result = CEItems.PUNCHED_TAPE.get().getDefaultInstance();
        setBytes(result, bytes);
        return result;
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            //TODO remove? replace with more sensible values?
            items.add(setBytes(new ItemStack(this), BitUtils.toBytesWithParity("Test1")));
            items.add(setBytes(new ItemStack(this), BitUtils.toBytesWithParity("Another test")));
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
                new TranslationTextComponent(Constants.PUNCHED_TAPE_BYTES, data.length)
        );
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(
            @Nonnull World worldIn, @Nonnull PlayerEntity playerIn, @Nonnull Hand handIn
    ) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) {
            //TODO name tapes?
            Minecraft.getInstance().displayGuiScreen(new ViewTapeScreen("Tape", getBytes(stack), handIn));
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
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
