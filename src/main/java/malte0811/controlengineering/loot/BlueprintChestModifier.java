package malte0811.controlengineering.loot;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import malte0811.controlengineering.ControlEngineering;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class BlueprintChestModifier extends LootModifier {
    public static final String SCOPE_COMPONENTS_BLUEPRINT = ControlEngineering.MODID + ":scope_components";

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTER = DeferredRegister.create(
            ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ControlEngineering.MODID
    );
    private static final RegistryObject<Codec<BlueprintChestModifier>> EXTRA_BLUEPRINT = REGISTER.register(
            "hemp_seed_drops", () -> RecordCodecBuilder.create(
                    inst -> codecStart(inst).apply(inst, BlueprintChestModifier::new)
            )
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
    public Codec<? extends IGlobalLootModifier> codec() {
        return EXTRA_BLUEPRINT.get();
    }
}
