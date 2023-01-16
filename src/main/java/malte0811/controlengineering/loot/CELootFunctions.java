package malte0811.controlengineering.loot;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.base.IHasMaster;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CELootFunctions {
    public static final DeferredRegister<LootPoolEntryType> REGISTER = DeferredRegister.create(
            Registries.LOOT_POOL_ENTRY_TYPE, ControlEngineering.MODID
    );

    public static final RegistryObject<LootPoolEntryType> B_ENTITY_DROP = registerEntry(
            ExtraBEDropEntry.ID, ExtraBEDropEntry.Serializer::new
    );
    public static final RegistryObject<LootPoolEntryType> CONTROL_PANEL = registerEntry(
            PanelDropEntry.ID, PanelDropEntry.Serializer::new
    );

    @Nullable
    public static BlockEntity getMasterBE(LootContext ctx) {
        if (!ctx.hasParam(LootContextParams.BLOCK_ENTITY)) {
            return null;
        }
        BlockEntity be = ctx.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (be instanceof IHasMaster<?> hasMaster) {
            return hasMaster.getOrComputeMasterBE(ctx.getParamOrNull(LootContextParams.BLOCK_STATE));
        } else { return be; }
    }

    private static RegistryObject<LootPoolEntryType> registerEntry(
            String id, Supplier<Serializer<? extends LootPoolEntryContainer>> serializer
    ) {
        return REGISTER.register(id, () -> new LootPoolEntryType(serializer.get()));
    }
}
