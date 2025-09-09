package malte0811.controlengineering.items;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteLists;
import malte0811.controlengineering.client.ClientHooks;
import malte0811.controlengineering.itemdata.CEDataComponents;
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
import java.util.List;

public class PunchedTapeItem extends Item {
    public static final String PUNCHED_TAPE_BYTES = "controlengineering.tooltip.written_tape_bytes";

    public PunchedTapeItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack withBytes(ByteList bytes) {
        ItemStack result = CEItems.PUNCHED_TAPE.get().getDefaultInstance();
        setBytes(result, bytes);
        return result;
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nonnull TooltipContext context,
            @Nonnull List<Component> tooltip,
            @Nonnull TooltipFlag flagIn
    ) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        ByteList data = getBytes(stack);
        TextUtil.addTooltipLine(tooltip, Component.translatable(PUNCHED_TAPE_BYTES, data.size()));
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

    public static ByteList getBytes(ItemStack tape) {
        return tape.getOrDefault(CEDataComponents.TAPE_CONTENT, ByteLists.emptyList());
    }

    public static ItemStack setBytes(ItemStack tape, ByteList newData) {
        tape.set(CEDataComponents.TAPE_CONTENT, new ByteArrayList(newData));
        return tape;
    }
}
