package malte0811.controlengineering.items;

import malte0811.controlengineering.client.ClientHooks;
import malte0811.controlengineering.util.ItemNBTUtil;
import malte0811.controlengineering.util.TextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
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
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack withBytes(byte[] bytes) {
        ItemStack result = CEItems.PUNCHED_TAPE.get().getDefaultInstance();
        setBytes(result, bytes);
        return result;
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
        TextUtil.addTooltipLine(tooltip, Component.translatable(PUNCHED_TAPE_BYTES, data.length));
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(
            @Nonnull Level worldIn, @Nonnull Player playerIn, @Nonnull InteractionHand handIn
    ) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (worldIn.isClientSide) {
            ClientHooks.openTape(getBytes(stack), handIn);
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
