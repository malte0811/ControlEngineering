package malte0811.controlengineering.blocks.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.base.IExtraDropBE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ExtraBEDropEntry extends LootPoolSingletonContainer {
    public static final ResourceLocation ID = new ResourceLocation(ControlEngineering.MODID, "extra_be_drop");

    protected ExtraBEDropEntry(
            int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn
    ) {
        super(weightIn, qualityIn, conditionsIn, functionsIn);
    }

    @Override
    protected void createItemStack(@Nonnull Consumer<ItemStack> output, @Nonnull LootContext context) {
        if (CELootFunctions.getMasterBE(context) instanceof IExtraDropBE extraDropBE) {
            extraDropBE.getExtraDrops(output);
        }
    }

    public static LootPoolSingletonContainer.Builder<?> builder() {
        return simpleBuilder(ExtraBEDropEntry::new);
    }

    @Nonnull
    @Override
    public LootPoolEntryType getType() {
        return CELootFunctions.bEntityDrop;
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<ExtraBEDropEntry> {
        @Nonnull
        @Override
        protected ExtraBEDropEntry deserialize(
                @Nonnull JsonObject json,
                @Nonnull JsonDeserializationContext context,
                int weight,
                int quality,
                @Nonnull LootItemCondition[] conditions,
                @Nonnull LootItemFunction[] functions
        ) {
            return new ExtraBEDropEntry(weight, quality, conditions, functions);
        }
    }
}
