package malte0811.controlengineering.network;

import blusunrize.immersiveengineering.api.utils.codec.IEStreamCodecs;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.util.ItemUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CutTapePacket(InteractionHand hand, int offset) implements IPacket {
    public static final CustomPacketPayload.Type<CutTapePacket> ID = IPacket.createType("cut_tape");
    public static final StreamCodec<FriendlyByteBuf, CutTapePacket> CODEC = StreamCodec.composite(
            IEStreamCodecs.enumStreamCodec(InteractionHand.values()), CutTapePacket::hand,
            ByteBufCodecs.VAR_INT, CutTapePacket::offset,
            CutTapePacket::new
    );

    @Override
    public void process(IPayloadContext ctx) {
        ServerPlayer player = IPacket.serverPlayer(ctx);
        if (!canCut(hand, player)) {
            return;
        }
        ByteList data = PunchedTapeItem.getBytes(player.getItemInHand(hand));
        if (offset < 0 || offset >= data.size()) {
            return;
        }
        ByteList startData = new ByteArrayList(data.subList(0, offset));
        ByteList endData = new ByteArrayList(data.subList(offset, data.size()));
        player.setItemInHand(hand, ItemStack.EMPTY);
        player.getItemInHand(otherHand(hand)).consume(1, player);
        giveTape(player, startData);
        giveTape(player, endData);
    }

    private void giveTape(Player player, ByteList data) {
        if (!data.isEmpty()) {
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
        return shears.is(Tags.Items.TOOLS_SHEAR);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
