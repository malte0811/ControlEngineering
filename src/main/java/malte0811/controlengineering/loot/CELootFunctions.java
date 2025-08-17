package malte0811.controlengineering.loot;

import com.mojang.serialization.MapCodec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.base.IHasMaster;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CELootFunctions {
    public static final DeferredRegister<LootPoolEntryType> REGISTER = DeferredRegister.create(
            Registries.LOOT_POOL_ENTRY_TYPE, ControlEngineering.MODID
    );

    public static final DeferredHolder<LootPoolEntryType, ?> B_ENTITY_DROP = registerEntry(
            ExtraBEDropEntry.ID, ExtraBEDropEntry.CODEC
    );
    public static final DeferredHolder<LootPoolEntryType, ?> CONTROL_PANEL = registerEntry(
            PanelDropEntry.ID, PanelDropEntry.CODEC
    );

    @Nullable
    public static BlockEntity getMasterBE(LootContext ctx) {
        if (!ctx.hasParam(LootContextParams.BLOCK_ENTITY)) {
            return null;
        }
        BlockEntity be = ctx.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (be instanceof IHasMaster<?> hasMaster) {
            return hasMaster.getOrComputeMasterBE(ctx.getParamOrNull(LootContextParams.BLOCK_STATE));
        } else {
            return be;
        }
    }

    private static DeferredHolder<LootPoolEntryType, ?> registerEntry(
            String id, MapCodec<? extends LootPoolEntryContainer> serializer
    ) {
        return REGISTER.register(id, () -> new LootPoolEntryType(serializer));
    }
}
