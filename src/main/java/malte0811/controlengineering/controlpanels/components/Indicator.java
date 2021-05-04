package malte0811.controlengineering.controlpanels.components;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.List;

public class Indicator extends PanelComponentType<ColorAndSignal, Integer> {
    public static final String TRANSLATION_KEY = ControlEngineering.MODID + ".component.indicator";

    public Indicator() {
        super(
                ColorAndSignal.DEFAULT, 0,
                ColorAndSignal.CODEC, Codec.INT,
                new Vec2i(1, 1),
                TRANSLATION_KEY
        );
    }

    @Override
    public BusState getEmittedState(ColorAndSignal colorAndSignal, Integer integer) {
        return BusState.EMPTY;
    }

    @Override
    public Integer updateTotalState(ColorAndSignal colorAndSignal, Integer oldState, BusState busState) {
        return busState.getSignal(colorAndSignal.getSignal());
    }

    @Override
    public Integer tick(ColorAndSignal colorAndSignal, Integer oldState) {
        return oldState;
    }

    @Override
    protected double getSelectionHeight() {
        return -1;
    }

    @Nonnull
    @Override
    protected List<IngredientWithSize> makeCostList() {
        return ImmutableList.of(
                new IngredientWithSize(Tags.Items.DYES),
                new IngredientWithSize(Tags.Items.DUSTS_GLOWSTONE)
        );
    }

    @Override
    public Pair<ActionResultType, Integer> click(ColorAndSignal colorAndSignal, Integer oldState) {
        return Pair.of(ActionResultType.PASS, oldState);
    }
}
