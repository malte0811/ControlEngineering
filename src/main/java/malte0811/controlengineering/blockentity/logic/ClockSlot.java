package malte0811.controlengineering.blockentity.logic;

import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.logic.clock.ClockGenerator;
import malte0811.controlengineering.logic.clock.ClockTypes;
import malte0811.controlengineering.util.ItemUtil;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class ClockSlot {
    @Nonnull
    private ClockGenerator.ClockInstance<?> clock = ClockTypes.NEVER.newInstance();

    public InteractionResult click(UseOnContext ctx, Runnable onSuccess) {
        if (ctx.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        ClockGenerator<?> currentClock = clock.getType();
        RegistryObject<Item> clockItem = CEItems.CLOCK_GENERATORS.get(currentClock.getRegistryName());
        if (!ctx.getLevel().isClientSide) {
            if (clockItem != null) {
                ItemUtil.giveOrDrop(ctx.getPlayer(), new ItemStack(clockItem.get()));
                clock = ClockTypes.NEVER.newInstance();
                onSuccess.run();
            } else {
                ItemStack item = ctx.getItemInHand();
                ClockGenerator<?> newClock = ClockTypes.REGISTRY.get(item.getItem().getRegistryName());
                if (newClock != null) {
                    clock = newClock.newInstance();
                    item.shrink(1);
                    onSuccess.run();
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nonnull
    public ClockGenerator.ClockInstance<?> getClock() {
        return clock;
    }

    public ClockGenerator<?> getType() {
        return clock.getType();
    }

    public boolean isPresent() {
        return clock.getType().isActiveClock();
    }

    public void load(Tag data) {
        this.clock = ClockGenerator.ClockInstance.CODEC.fromNBT(data, ClockTypes.NEVER::newInstance);
    }

    public Tag toNBT() {
        return ClockGenerator.ClockInstance.CODEC.toNBT(clock);
    }

    public Tag toClientNBT() {
        return ByteTag.valueOf(isPresent());
    }

    public void loadClientNBT(Tag data) {
        if (data instanceof ByteTag byteTag && byteTag.getAsByte() != 0) {
            clock = ClockTypes.ALWAYS_ON.newInstance();
        } else {
            clock = ClockTypes.NEVER.newInstance();
        }
    }
}
