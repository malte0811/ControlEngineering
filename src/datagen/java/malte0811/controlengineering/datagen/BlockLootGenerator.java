package malte0811.controlengineering.datagen;

import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.loot.ExtraBEDropEntry;
import malte0811.controlengineering.loot.PanelDropEntry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class BlockLootGenerator implements LootTableSubProvider {
    private final Map<ResourceLocation, LootTable.Builder> tables = new HashMap<>();

    public BlockLootGenerator(HolderLookup.Provider provider) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> out) {
        registerSelfDropping(CEBlocks.LOGIC_CABINET, bEntityDrops());
        registerSelfDropping(CEBlocks.PANEL_CNC, bEntityDrops());
        registerSelfDropping(CEBlocks.KEYPUNCH, bEntityDrops());
        register(CEBlocks.CONTROL_PANEL, createPoolBuilder().add(PanelDropEntry.builder()));
        registerSelfDropping(CEBlocks.LOGIC_WORKBENCH, bEntityDrops());
        registerSelfDropping(CEBlocks.SCOPE, bEntityDrops());
        registerAllRemainingAsDefault();
        tables.forEach((rl, builder) -> out.accept(ResourceKey.create(Registries.LOOT_TABLE, rl), builder));
    }

    private void registerAllRemainingAsDefault() {
        for (DeferredHolder<Block, ? extends Block> b : CEBlocks.REGISTER.getEntries())
            if (!tables.containsKey(toTableLoc(b)))
                registerSelfDropping(b);
    }

    private void registerSelfDropping(DeferredHolder<Block, ?> b, LootPool.Builder... pool) {
        LootPool.Builder[] withSelf = Arrays.copyOf(pool, pool.length + 1);
        withSelf[withSelf.length - 1] = singleItem(b.get());
        register(b, withSelf);
    }

    private LootPool.Builder bEntityDrops() {
        return createPoolBuilder()
                .add(ExtraBEDropEntry.builder());
    }

    private LootPool.Builder singleItem(ItemLike in) {
        return createPoolBuilder()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(in));
    }

    private LootPool.Builder createPoolBuilder() {
        return LootPool.lootPool().when(ExplosionCondition.survivesExplosion());
    }

    private void register(DeferredHolder<Block, ?> b, LootPool.Builder... pools) {
        LootTable.Builder builder = LootTable.lootTable();
        for (LootPool.Builder pool : pools)
            builder.withPool(pool);
        register(b, builder);
    }

    private void register(DeferredHolder<Block, ?> b, LootTable.Builder table) {
        register(b.getId(), table);
    }

    private void register(ResourceLocation name, LootTable.Builder table) {
        if (tables.put(toTableLoc(name), table.setParamSet(LootContextParamSets.BLOCK)) != null)
            throw new IllegalStateException("Duplicate loot table " + name);
    }

    private ResourceLocation toTableLoc(DeferredHolder<Block, ?> in) {
        return toTableLoc(in.getId());
    }

    private ResourceLocation toTableLoc(ResourceLocation in) {
        return in.withPrefix("blocks/");
    }
}
