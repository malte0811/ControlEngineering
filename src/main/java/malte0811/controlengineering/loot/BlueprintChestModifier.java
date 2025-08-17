package malte0811.controlengineering.loot;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import malte0811.controlengineering.ControlEngineering;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import javax.annotation.Nonnull;

public class BlueprintChestModifier extends LootModifier {
    public static final String SCOPE_COMPONENTS_BLUEPRINT = ControlEngineering.MODID + ":scope_components";

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTER = DeferredRegister.create(
            NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ControlEngineering.MODID
    );
    private static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<BlueprintChestModifier>> EXTRA_BLUEPRINT = REGISTER.register(
            "hemp_seed_drops", () -> RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, BlueprintChestModifier::new))
    );

    public BlueprintChestModifier(LootItemCondition... conditionsIn) {
        super(conditionsIn);
    }

    @Override
    @Nonnull
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(BlueprintCraftingRecipe.getTypedBlueprint(SCOPE_COMPONENTS_BLUEPRINT));
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return EXTRA_BLUEPRINT.get();
    }
}
