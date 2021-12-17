package malte0811.controlengineering.blocks.loot;

import malte0811.controlengineering.blockentity.base.IHasMaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.Nullable;

public class CELootFunctions {
    public static LootPoolEntryType bEntityDrop;
    public static LootPoolEntryType controlPanel;

    public static void register() {
        bEntityDrop = registerEntry(ExtraBEDropEntry.ID, new ExtraBEDropEntry.Serializer());
        controlPanel = registerEntry(PanelDropEntry.ID, new PanelDropEntry.Serializer());
    }

    private static LootPoolEntryType registerEntry(
            ResourceLocation id,
            Serializer<? extends LootPoolEntryContainer> serializer
    ) {
        return Registry.register(
                Registry.LOOT_POOL_ENTRY_TYPE, id, new LootPoolEntryType(serializer)
        );
    }

    @Nullable
    public static BlockEntity getMasterBE(LootContext ctx) {
        if (!ctx.hasParam(LootContextParams.BLOCK_ENTITY)) {
            return null;
        }
        BlockEntity be = ctx.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (be instanceof IHasMaster hasMaster)
            return hasMaster.getOrComputeMasterBE(ctx.getParamOrNull(LootContextParams.BLOCK_STATE));
        else
            return be;
    }
}
