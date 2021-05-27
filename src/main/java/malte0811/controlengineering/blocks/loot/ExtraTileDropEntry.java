package malte0811.controlengineering.blocks.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.tiles.base.IExtraDropTile;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ExtraTileDropEntry extends StandaloneLootEntry {
    public static final ResourceLocation ID = new ResourceLocation(ControlEngineering.MODID, "extra_tile_drop");

    protected ExtraTileDropEntry(
            int weightIn,
            int qualityIn,
            ILootCondition[] conditionsIn,
            ILootFunction[] functionsIn
    ) {
        super(weightIn, qualityIn, conditionsIn, functionsIn);
    }

    @Override
    protected void func_216154_a(@Nonnull Consumer<ItemStack> output, @Nonnull LootContext context) {
        TileEntity te = CELootFunctions.getMasterTile(context);
        if (te instanceof IExtraDropTile) {
            ((IExtraDropTile) te).getExtraDrops(output);
        }
    }

    public static StandaloneLootEntry.Builder<?> builder() {
        return builder(ExtraTileDropEntry::new);
    }

    @Nonnull
    @Override
    public LootPoolEntryType func_230420_a_() {
        return CELootFunctions.tileDrop;
    }

    public static class Serializer extends StandaloneLootEntry.Serializer<ExtraTileDropEntry> {
        @Nonnull
        @Override
        protected ExtraTileDropEntry deserialize(
                @Nonnull JsonObject json,
                @Nonnull JsonDeserializationContext context,
                int weight,
                int quality,
                @Nonnull ILootCondition[] conditions,
                @Nonnull ILootFunction[] functions
        ) {
            return new ExtraTileDropEntry(weight, quality, conditions, functions);
        }
    }
}
