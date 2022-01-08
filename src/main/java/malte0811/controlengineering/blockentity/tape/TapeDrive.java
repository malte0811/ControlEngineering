package malte0811.controlengineering.blockentity.tape;

import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.util.ItemUtil;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class TapeDrive {
    private final Runnable onAdd;
    private final Runnable onRemove;
    private final BooleanSupplier canTake;
    @Nullable
    private byte[] insertedTape = null;

    public TapeDrive(Runnable onAdd, Runnable onRemove, BooleanSupplier canTake) {
        this.onAdd = onAdd;
        this.onRemove = onRemove;
        this.canTake = canTake;
    }

    public InteractionResult click(UseOnContext ctx) {
        final ItemStack held = ctx.getItemInHand();
        var level = ctx.getLevel();
        if (insertedTape == null) {
            if (CEItems.PUNCHED_TAPE.get() == held.getItem()) {
                if (!level.isClientSide) {
                    insertedTape = PunchedTapeItem.getBytes(held);
                    onAdd.run();
                    Player player = ctx.getPlayer();
                    if (player == null || !player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        } else if (canTake.getAsBoolean()) {
            if (!level.isClientSide) {
                ItemStack result = PunchedTapeItem.withBytes(insertedTape);
                insertedTape = null;
                onRemove.run();
                ItemUtil.giveOrDrop(ctx.getPlayer(), result);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public byte[] getTapeContent() {
        return Objects.requireNonNull(insertedTape);
    }

    @Nullable
    public byte[] getNullableTapeContent() {
        return insertedTape;
    }

    public boolean hasTape() {
        return insertedTape != null;
    }

    public int getTapeLength() {
        return hasTape() ? getTapeContent().length : 0;
    }

    public Tag toNBT() {
        if (hasTape()) {
            return new ByteArrayTag(getTapeContent());
        } else {
            return new ByteArrayTag(new byte[0]);
        }
    }

    public void loadNBT(Tag data) {
        if (data instanceof ByteArrayTag bat && bat.getAsByteArray().length > 0) {
            this.insertedTape = bat.getAsByteArray();
        } else {
            this.insertedTape = null;
        }
    }

    public void loadClientNBT(Tag syncTape) {
        if (syncTape instanceof NumericTag numeric && numeric.getAsInt() > 0) {
            this.insertedTape = new byte[numeric.getAsInt()];
        } else {
            this.insertedTape = null;
        }
    }

    public Tag toClientNBT() {
        return IntTag.valueOf(getTapeLength());
    }
}
