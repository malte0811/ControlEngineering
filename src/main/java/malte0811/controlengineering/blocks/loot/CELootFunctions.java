package malte0811.controlengineering.blocks.loot;

import malte0811.controlengineering.tiles.base.IHasMaster;
import net.minecraft.loot.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

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
            ILootSerializer<? extends LootEntry> serializer
    ) {
        return Registry.register(
                Registry.LOOT_POOL_ENTRY_TYPE, id, new LootPoolEntryType(serializer)
        );
    }

    @Nullable
    public static TileEntity getMasterTile(LootContext ctx) {
        if (!ctx.has(LootParameters.BLOCK_ENTITY)) {
            return null;
        }
        TileEntity te = ctx.get(LootParameters.BLOCK_ENTITY);
        if (te instanceof IHasMaster)
            return ((IHasMaster) te).getOrComputeMasterTile(ctx.get(LootParameters.BLOCK_STATE));
        else
            return te;
    }
}
