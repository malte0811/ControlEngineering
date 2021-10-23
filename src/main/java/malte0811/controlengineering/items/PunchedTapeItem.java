package malte0811.controlengineering.items;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.tape.ViewTapeScreen;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.ItemNBTUtil;
import malte0811.controlengineering.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PunchedTapeItem extends Item {
    // Main tag
    private static final String BYTES_KEY = "bytes";
    public static final String PUNCHED_TAPE_BYTES = "controlengineering.tooltip.written_tape_bytes";

    public PunchedTapeItem() {
        super(new Item.Properties().tab(ControlEngineering.ITEM_GROUP).stacksTo(1));
    }

    public static ItemStack withBytes(byte[] bytes) {
        ItemStack result = CEItems.PUNCHED_TAPE.get().getDefaultInstance();
        setBytes(result, bytes);
        return result;
    }

    @Override
    public void fillItemCategory(@Nonnull CreativeModeTab group, @Nonnull NonNullList<ItemStack> items) {
        if (allowdedIn(group)) {
            //TODO remove? replace with more sensible values?
            items.add(setBytes(new ItemStack(this), BitUtils.toBytesWithParity("Test1")));
            items.add(setBytes(new ItemStack(this), BitUtils.toBytesWithParity("Another test")));
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
        byte[] data = getBytes(stack);
        TextUtil.addTooltipLine(tooltip, new TranslatableComponent(PUNCHED_TAPE_BYTES, data.length));
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(
            @Nonnull Level worldIn, @Nonnull Player playerIn, @Nonnull InteractionHand handIn
    ) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (worldIn.isClientSide) {
            //TODO name tapes?
            Minecraft.getInstance().setScreen(new ViewTapeScreen("Tape", getBytes(stack), handIn));
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
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
