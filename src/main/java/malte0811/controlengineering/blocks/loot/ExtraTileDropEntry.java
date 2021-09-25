package malte0811.controlengineering.blocks.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.tiles.base.IExtraDropTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ExtraTileDropEntry extends LootPoolSingletonContainer {
    public static final ResourceLocation ID = new ResourceLocation(ControlEngineering.MODID, "extra_tile_drop");

    protected ExtraTileDropEntry(
            int weightIn,
            int qualityIn,
            LootItemCondition[] conditionsIn,
            LootItemFunction[] functionsIn
    ) {
        super(weightIn, qualityIn, conditionsIn, functionsIn);
    }

    @Override
    protected void createItemStack(@Nonnull Consumer<ItemStack> output, @Nonnull LootContext context) {
        BlockEntity te = CELootFunctions.getMasterTile(context);
        if (te instanceof IExtraDropTile) {
            ((IExtraDropTile) te).getExtraDrops(output);
        }
    }

    public static LootPoolSingletonContainer.Builder<?> builder() {
        return simpleBuilder(ExtraTileDropEntry::new);
    }

    @Nonnull
    @Override
    public LootPoolEntryType getType() {
        return CELootFunctions.tileDrop;
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<ExtraTileDropEntry> {
        @Nonnull
        @Override
        protected ExtraTileDropEntry deserialize(
                @Nonnull JsonObject json,
                @Nonnull JsonDeserializationContext context,
                int weight,
                int quality,
                @Nonnull LootItemCondition[] conditions,
                @Nonnull LootItemFunction[] functions
        ) {
            return new ExtraTileDropEntry(weight, quality, conditions, functions);
        }
    }
}
