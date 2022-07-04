package malte0811.controlengineering.blocks.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import malte0811.controlengineering.blockentity.panels.ControlPanelBlockEntity;
import malte0811.controlengineering.blocks.CEBlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class PanelDropEntry extends LootPoolSingletonContainer {
    public static final String ID = "panel";

    protected PanelDropEntry(int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn) {
        super(weightIn, qualityIn, conditionsIn, functionsIn);
    }

    @Override
    protected void createItemStack(@Nonnull Consumer<ItemStack> stackConsumer, @Nonnull LootContext context) {
        if (CELootFunctions.getMasterBE(context) instanceof ControlPanelBlockEntity panel) {
            CompoundTag tag = panel.getData().copy(true).toNBT();
            ItemStack toDrop = new ItemStack(CEBlocks.CONTROL_PANEL.get(), 1);
            toDrop.setTag(tag);
            stackConsumer.accept(toDrop);
        }
    }

    @Nonnull
    @Override
    public LootPoolEntryType getType() {
        return CELootFunctions.CONTROL_PANEL.get();
    }

    public static LootPoolSingletonContainer.Builder<?> builder() {
        return simpleBuilder(PanelDropEntry::new);
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<PanelDropEntry> {
        @Nonnull
        @Override
        protected PanelDropEntry deserialize(
                @Nonnull JsonObject object,
                @Nonnull JsonDeserializationContext context,
                int weight,
                int quality,
                @Nonnull LootItemCondition[] conditions,
                @Nonnull LootItemFunction[] functions
        ) {
            return new PanelDropEntry(weight, quality, conditions, functions);
        }
    }
}
