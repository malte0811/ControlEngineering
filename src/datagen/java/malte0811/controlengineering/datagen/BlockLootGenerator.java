package malte0811.controlengineering.datagen;

import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.loot.ExtraTileDropEntry;
import malte0811.controlengineering.blocks.loot.PanelDropEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.fmllegacy.RegistryObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlockLootGenerator implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
    private final Map<ResourceLocation, LootTable.Builder> tables = new HashMap<>();

    @Override
    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> out) {
        //TODO
        // logic workbench => parts in drawers
        // panel designer => ???????
        registerSelfDropping(CEBlocks.LOGIC_CABINET, tileDrop());
        registerSelfDropping(CEBlocks.PANEL_CNC, tileDrop());
        registerSelfDropping(CEBlocks.KEYPUNCH, tileDrop());
        register(CEBlocks.CONTROL_PANEL, createPoolBuilder().add(PanelDropEntry.builder()));
        registerAllRemainingAsDefault();
        tables.forEach(out);
    }

    private void registerAllRemainingAsDefault() {
        for (RegistryObject<Block> b : CEBlocks.REGISTER.getEntries())
            if (!tables.containsKey(toTableLoc(b)))
                registerSelfDropping(b);
    }

    private void registerSelfDropping(RegistryObject<? extends Block> b, LootPool.Builder... pool) {
        LootPool.Builder[] withSelf = Arrays.copyOf(pool, pool.length + 1);
        withSelf[withSelf.length - 1] = singleItem(b.get());
        register(b, withSelf);
    }

    private LootPool.Builder tileDrop() {
        return createPoolBuilder()
                .add(ExtraTileDropEntry.builder());
    }

    private LootPool.Builder singleItem(ItemLike in) {
        return createPoolBuilder()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(in));
    }

    private LootPool.Builder createPoolBuilder() {
        return LootPool.lootPool().when(ExplosionCondition.survivesExplosion());
    }

    private void register(RegistryObject<? extends Block> b, LootPool.Builder... pools) {
        LootTable.Builder builder = LootTable.lootTable();
        for (LootPool.Builder pool : pools)
            builder.withPool(pool);
        register(b, builder);
    }

    private void register(RegistryObject<? extends Block> b, LootTable.Builder table) {
        register(b.getId(), table);
    }

    private void register(ResourceLocation name, LootTable.Builder table) {
        if (tables.put(toTableLoc(name), table.setParamSet(LootContextParamSets.BLOCK)) != null)
            throw new IllegalStateException("Duplicate loot table " + name);
    }

    private ResourceLocation toTableLoc(RegistryObject<? extends Block> in) {
        return toTableLoc(in.getId());
    }

    private ResourceLocation toTableLoc(ResourceLocation in) {
        return new ResourceLocation(in.getNamespace(), "blocks/" + in.getPath());
    }
}
