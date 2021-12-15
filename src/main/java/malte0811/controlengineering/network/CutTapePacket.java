package malte0811.controlengineering.network;

import blusunrize.immersiveengineering.api.ApiUtils;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.util.ItemUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;

public class CutTapePacket extends SimplePacket {
    private final InteractionHand hand;
    private final int offset;

    public CutTapePacket(InteractionHand hand, int offset) {
        this.hand = hand;
        this.offset = offset;
    }

    public CutTapePacket(FriendlyByteBuf in) {
        this(in.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, in.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf out) {
        out.writeBoolean(hand == InteractionHand.MAIN_HAND);
        out.writeVarInt(offset);
    }

    @Override
    protected void processOnThread(NetworkEvent.Context ctx) {
        ServerPlayer player = Objects.requireNonNull(ctx.getSender());
        if (!canCut(hand, player)) {
            return;
        }
        byte[] data = PunchedTapeItem.getBytes(player.getItemInHand(hand));
        if (offset < 0 || offset >= data.length) {
            return;
        }
        byte[] startData = new byte[offset];
        byte[] endData = new byte[data.length - offset - 1];
        System.arraycopy(data, 0, startData, 0, offset);
        System.arraycopy(data, offset + 1, endData, 0, endData.length);
        player.setItemInHand(hand, ItemStack.EMPTY);
        player.getItemInHand(otherHand(hand)).hurt(1, ApiUtils.RANDOM, player);
        giveTape(player, startData);
        giveTape(player, endData);
    }

    private void giveTape(Player player, byte[] data) {
        if (data.length > 0) {
            ItemUtil.giveOrDrop(player, PunchedTapeItem.withBytes(data));
        }
    }

    private static InteractionHand otherHand(InteractionHand in) {
        return in == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    public static boolean canCut(InteractionHand tapeHand, Player player) {
        if (player.getItemInHand(tapeHand).getItem() != CEItems.PUNCHED_TAPE.get()) {
            return false;
        }
        ItemStack shears = player.getItemInHand(otherHand(tapeHand));
        return Tags.Items.SHEARS.contains(shears.getItem());
    }
}
