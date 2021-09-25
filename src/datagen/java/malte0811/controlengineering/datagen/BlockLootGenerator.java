package malte0811.controlengineering.datagen;

import blusunrize.immersiveengineering.data.loot.LootGenerator;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.loot.ExtraTileDropEntry;
import malte0811.controlengineering.blocks.loot.PanelDropEntry;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class BlockLootGenerator extends LootGenerator {
    public BlockLootGenerator(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void registerTables() {
        //TODO
        // logic workbench => parts in drawers
        // panel designer => ???????
        registerSelfDropping(CEBlocks.LOGIC_CABINET, tileDrop());
        registerSelfDropping(CEBlocks.PANEL_CNC, tileDrop());
        registerSelfDropping(CEBlocks.KEYPUNCH, tileDrop());
        register(CEBlocks.CONTROL_PANEL, createPoolBuilder().addEntry(PanelDropEntry.builder()));
        registerAllRemainingAsDefault();
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
                .addEntry(ExtraTileDropEntry.builder());
    }

    private LootPool.Builder singleItem(IItemProvider in) {
        return createPoolBuilder()
                .rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(in));
    }

    private LootPool.Builder createPoolBuilder() {
        return LootPool.builder().acceptCondition(SurvivesExplosion.builder());
    }

    private void register(RegistryObject<? extends Block> b, LootPool.Builder... pools) {
        LootTable.Builder builder = LootTable.builder();
        for (LootPool.Builder pool : pools)
            builder.addLootPool(pool);
        register(b, builder);
    }

    private void register(RegistryObject<? extends Block> b, LootTable.Builder table) {
        register(b.getId(), table);
    }

    private void register(ResourceLocation name, LootTable.Builder table) {
        if (tables.put(toTableLoc(name), table.setParameterSet(LootParameterSets.BLOCK).build()) != null)
            throw new IllegalStateException("Duplicate loot table " + name);
    }

    private ResourceLocation toTableLoc(RegistryObject<? extends Block> in) {
        return toTableLoc(in.getId());
    }

    private ResourceLocation toTableLoc(ResourceLocation in) {
        return new ResourceLocation(in.getNamespace(), "blocks/" + in.getPath());
    }

    @Nonnull
    @Override
    public String getName() {
        return "Block loot";
    }
}
