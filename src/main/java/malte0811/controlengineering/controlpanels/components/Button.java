package malte0811.controlengineering.controlpanels.components;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.List;

public class Button extends PanelComponentType<ColorAndSignal, Boolean> {
    public static final String TRANSLATION_KEY = ControlEngineering.MODID + ".component.button";

    public Button() {
        super(
                ColorAndSignal.DEFAULT, false,
                ColorAndSignal.CODEC, Codec.BOOL,
                new Vec2d(1, 1),
                TRANSLATION_KEY
        );
    }

    @Override
    public BusState getEmittedState(ColorAndSignal config, Boolean active) {
        if (active) {
            return config.getSignal().singleSignalState(BusLine.MAX_VALID_VALUE);
        } else {
            return BusState.EMPTY;
        }
    }

    @Override
    public Boolean updateTotalState(ColorAndSignal config, Boolean oldState, BusState busState) {
        return oldState;
    }

    @Override
    public Boolean tick(ColorAndSignal config, Boolean oldState) {
        return oldState;
    }

    @Override
    protected double getSelectionHeight() {
        return 0.5;
    }

    @Nonnull
    @Override
    protected List<IngredientWithSize> makeCostList() {
        return ImmutableList.of(
                new IngredientWithSize(Tags.Items.DYES),
                new IngredientWithSize(Tags.Items.DUSTS_GLOWSTONE),
                new IngredientWithSize(Ingredient.of(Items.STONE_BUTTON))
        );
    }

    @Override
    public Pair<InteractionResult, Boolean> click(ColorAndSignal config, Boolean oldState) {
        return Pair.of(InteractionResult.SUCCESS, !oldState);
    }
}
