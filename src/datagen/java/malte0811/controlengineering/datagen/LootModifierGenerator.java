package malte0811.controlengineering.datagen;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.loot.BlueprintChestModifier;
import malte0811.controlengineering.util.RLUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class LootModifierGenerator extends GlobalLootModifierProvider {
    public LootModifierGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, ControlEngineering.MODID);
    }

    @Override
    protected void start() {
        add("scope_blueprint", new BlueprintChestModifier(
                LootTableIdCondition.builder(RLUtils.ieLoc("chests/engineers_house")).build(),
                // 144: Total weight in basic loot table; 4: weight of blueprints; 4: number of rolls
                LootItemRandomChanceCondition.randomChance(4 * 4 / 144f).build()
        ));
    }
}
