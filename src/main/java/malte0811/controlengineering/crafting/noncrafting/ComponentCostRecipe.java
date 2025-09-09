package malte0811.controlengineering.crafting.noncrafting;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonObject;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.CERecipeTypes;
import malte0811.controlengineering.network.PacketUtils;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ComponentCostRecipe extends BaseRecipe {
    public static final DualMapCodec<RegistryFriendlyByteBuf, ComponentCostRecipe> CODECS = IngredientWithSize.CODECS
            .listOf()
            .map(ComponentCostRecipe::new, ComponentCostRecipe::getCost)
            .fieldOf("costs");

    private final List<IngredientWithSize> cost;

    public ComponentCostRecipe(List<IngredientWithSize> cost) {
        super(CERecipeSerializers.COMPONENT_COST, CERecipeTypes.COMPONENT_COST.get());
        this.cost = cost;
    }

    public List<IngredientWithSize> getCost() {
        return cost;
    }
}
