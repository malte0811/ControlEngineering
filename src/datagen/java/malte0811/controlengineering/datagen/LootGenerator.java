package malte0811.controlengineering.datagen;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LootGenerator extends LootTableProvider {
    public LootGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, Set.of(), List.of(), provider);
    }

    @Nonnull
    @Override
    public List<SubProviderEntry> getTables() {
        return ImmutableList.of(new SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK));
    }

    @Override
    protected void validate(
            WritableRegistry<LootTable> writableregistry,
            ValidationContext validationcontext,
            ProblemReporter.Collector problemreporter$collector
    ) {
        writableregistry.forEach(table -> table.validate(validationcontext));
    }
}
