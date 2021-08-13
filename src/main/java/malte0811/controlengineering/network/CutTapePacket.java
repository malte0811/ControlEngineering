package malte0811.controlengineering.network;

import blusunrize.immersiveengineering.api.ApiUtils;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.util.ItemUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;

public class CutTapePacket extends SimplePacket {
    private final Hand hand;
    private final int offset;

    public CutTapePacket(Hand hand, int offset) {
        this.hand = hand;
        this.offset = offset;
    }

    public CutTapePacket(PacketBuffer in) {
        this(in.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND, in.readVarInt());
    }

    @Override
    public void write(PacketBuffer out) {
        out.writeBoolean(hand == Hand.MAIN_HAND);
        out.writeVarInt(offset);
    }

    @Override
    protected void processOnThread(NetworkEvent.Context ctx) {
        ServerPlayerEntity player = Objects.requireNonNull(ctx.getSender());
        if (!canCut(hand, player)) {
            return;
        }
        byte[] data = PunchedTapeItem.getBytes(player.getHeldItem(hand));
        if (offset < 0 || offset >= data.length) {
            return;
        }
        byte[] startData = new byte[offset];
        byte[] endData = new byte[data.length - offset - 1];
        System.arraycopy(data, 0, startData, 0, offset);
        System.arraycopy(data, offset + 1, endData, 0, endData.length);
        player.setHeldItem(hand, ItemStack.EMPTY);
        player.getHeldItem(otherHand(hand)).attemptDamageItem(1, ApiUtils.RANDOM, player);
        giveTape(player, startData);
        giveTape(player, endData);
    }

    private void giveTape(PlayerEntity player, byte[] data) {
        if (data.length > 0) {
            ItemUtil.giveOrDrop(player, PunchedTapeItem.withBytes(data));
        }
    }

    private static Hand otherHand(Hand in) {
        return in == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    public static boolean canCut(Hand tapeHand, PlayerEntity player) {
        if (player.getHeldItem(tapeHand).getItem() != CEItems.PUNCHED_TAPE.get()) {
            return false;
        }
        ItemStack shears = player.getHeldItem(otherHand(tapeHand));
        return Tags.Items.SHEARS.contains(shears.getItem());
    }
}
