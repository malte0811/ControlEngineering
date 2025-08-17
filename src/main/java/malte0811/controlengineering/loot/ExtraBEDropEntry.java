package malte0811.controlengineering.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.blockentity.base.IExtraDropBE;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class ExtraBEDropEntry extends LootPoolSingletonContainer {
    public static final String ID = "extra_be_drop";
    public static final MapCodec<ExtraBEDropEntry> CODEC = RecordCodecBuilder.mapCodec(
            inst -> singletonFields(inst).apply(inst, ExtraBEDropEntry::new)
    );

    protected ExtraBEDropEntry(int weightIn, int qualityIn, List<LootItemCondition> conditionsIn, List<LootItemFunction> functionsIn)
    {
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
        return CELootFunctions.B_ENTITY_DROP.get();
    }
}
