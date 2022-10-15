package malte0811.controlengineering.datagen;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.loot.BlueprintChestModifier;
import malte0811.controlengineering.util.RLUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

public class LootModifierGenerator extends GlobalLootModifierProvider {
    public LootModifierGenerator(DataGenerator gen) {
        super(gen, ControlEngineering.MODID);
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
