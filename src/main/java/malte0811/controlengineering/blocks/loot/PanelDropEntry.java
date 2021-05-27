package malte0811.controlengineering.blocks.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class PanelDropEntry extends StandaloneLootEntry {
    public static final ResourceLocation ID = new ResourceLocation(ControlEngineering.MODID, "panel");

    protected PanelDropEntry(int weightIn, int qualityIn, ILootCondition[] conditionsIn, ILootFunction[] functionsIn) {
        super(weightIn, qualityIn, conditionsIn, functionsIn);
    }

    @Override
    protected void func_216154_a(@Nonnull Consumer<ItemStack> stackConsumer, @Nonnull LootContext context) {
        TileEntity tile = CELootFunctions.getMasterTile(context);
        if (tile instanceof ControlPanelTile) {
            CompoundNBT tag = ((ControlPanelTile) tile).getData().copy(true).toNBT();
            ItemStack toDrop = new ItemStack(CEBlocks.CONTROL_PANEL.get(), 1);
            toDrop.setTag(tag);
            stackConsumer.accept(toDrop);
        }
    }

    @Nonnull
    @Override
    public LootPoolEntryType func_230420_a_() {
        return CELootFunctions.controlPanel;
    }

    public static StandaloneLootEntry.Builder<?> builder() {
        return builder(PanelDropEntry::new);
    }

    public static class Serializer extends StandaloneLootEntry.Serializer<PanelDropEntry> {
        @Nonnull
        @Override
        protected PanelDropEntry deserialize(
                @Nonnull JsonObject object,
                @Nonnull JsonDeserializationContext context,
                int weight,
                int quality,
                @Nonnull ILootCondition[] conditions,
                @Nonnull ILootFunction[] functions
        ) {
            return new PanelDropEntry(weight, quality, conditions, functions);
        }
    }
}
