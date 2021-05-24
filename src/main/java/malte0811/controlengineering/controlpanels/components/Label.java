package malte0811.controlengineering.controlpanels.components;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import malte0811.controlengineering.util.ServerFontWidth;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class Label extends PanelComponentType<ColorAndText, Unit> {
    public static final String TRANSLATION_KEY = ControlEngineering.MODID + ".component.label";
    public static final int FONT_HEIGHT = 9;
    public static final float SCALE = 1f / FONT_HEIGHT;

    public Label() {
        super(
                ColorAndText.DEFAULT, Unit.INSTANCE,
                ColorAndText.CODEC, Codec.unit(Unit.INSTANCE),
                new Vec2d(1, 1), TRANSLATION_KEY
        );
    }

    @Override
    public BusState getEmittedState(ColorAndText s, Unit unit) {
        return BusState.EMPTY;
    }

    @Override
    public Unit updateTotalState(ColorAndText s, Unit oldState, BusState busState) {
        return oldState;
    }

    @Override
    public Unit tick(ColorAndText s, Unit oldState) {
        return oldState;
    }

    @Override
    public Pair<ActionResultType, Unit> click(ColorAndText s, Unit oldState) {
        return Pair.of(ActionResultType.PASS, oldState);
    }

    @Override
    protected double getSelectionHeight() {
        return 0;
    }

    @Nonnull
    @Override
    protected List<IngredientWithSize> makeCostList() {
        return ImmutableList.of(
                new IngredientWithSize(Tags.Items.DYES_BLACK),
                //TODO tag?
                new IngredientWithSize(Ingredient.fromItems(Items.PAPER))
        );
    }

    @Nullable
    @Override
    public AxisAlignedBB getSelectionShape() {
        return null;
    }

    @Override
    public Vec2d getSize(ColorAndText s) {
        // TODO handle client-side on dedicated servers?
        return new Vec2d(SCALE * ServerFontWidth.getWidth(s.getText()), 1);
    }
}
