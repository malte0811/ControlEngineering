package malte0811.controlengineering.blocks.loot;

import malte0811.controlengineering.tiles.base.IHasMaster;
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
    public static LootPoolEntryType tileDrop;
    public static LootPoolEntryType controlPanel;

    public static void register() {
        tileDrop = registerEntry(ExtraTileDropEntry.ID, new ExtraTileDropEntry.Serializer());
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
    public static BlockEntity getMasterTile(LootContext ctx) {
        if (!ctx.hasParam(LootContextParams.BLOCK_ENTITY)) {
            return null;
        }
        BlockEntity te = ctx.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (te instanceof IHasMaster)
            return ((IHasMaster) te).getOrComputeMasterTile(ctx.getParamOrNull(LootContextParams.BLOCK_STATE));
        else
            return te;
    }
}
