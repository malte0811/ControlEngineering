package malte0811.controlengineering.controlpanels;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.serialization.Codecs;
import malte0811.controlengineering.util.serialization.serial.SerialCodecParser;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class PanelComponentType<Config, State>
        extends TypedRegistryEntry<Pair<Config, State>, PanelComponentInstance<Config, State>> {
    private final Vec2d size;
    private final SerialCodecParser<Config> configParser;
    private final String translationKey;

    protected PanelComponentType(
            Config defaultConfig, State intitialState,
            Codec<Config> codecConfig, Codec<State> codecState,
            Vec2d size,
            String translationKey
    ) {
        super(Pair.of(defaultConfig, intitialState), Codecs.safePair(codecConfig, codecState));
        this.size = size;
        this.configParser = SerialCodecParser.getParser(codecConfig);
        this.translationKey = translationKey;
    }

    @Override
    public PanelComponentInstance<Config, State> newInstance(Pair<Config, State> state) {
        return new PanelComponentInstance<>(this, state);
    }

    public PanelComponentInstance<Config, State> newInstanceFromCfg(Config config) {
        return new PanelComponentInstance<>(this, Pair.of(config, getInitialState().getSecond()));
    }

    @Nullable
    public PanelComponentInstance<Config, State> newInstance(FriendlyByteBuf from) {
        return configParser.parse(from).map(this::newInstanceFromCfg).orElse(null);
    }

    public FastDataResult<PanelComponentInstance<Config, State>> newInstance(List<String> data) {
        return configParser.parse(data).map(this::newInstanceFromCfg);
    }

    public SerialCodecParser<Config> getConfigParser() {
        return configParser;
    }

    public List<String> toCNCStrings(Config config) {
        return configParser.stringify(config);
    }

    public abstract BusState getEmittedState(Config config, State state);

    public abstract State updateTotalState(Config config, State oldState, BusState busState);

    public abstract State tick(Config config, State oldState);

    public abstract Pair<InteractionResult, State> click(Config config, State oldState);

    protected abstract double getSelectionHeight();

    private final Lazy<AABB> defaultSelectionShape = Lazy.of(() -> {
        final double height = getSelectionHeight();
        if (height >= 0) {
            final Vec2d size = getSize(getInitialState().getFirst());
            return new AABB(0, 0, 0, size.x(), height, size.y());
        } else {
            return null;
        }
    });

    @Nullable
    public AABB getSelectionShape() {
        return defaultSelectionShape.get();
    }

    public Vec2d getSize(Config config) {
        return size;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    private List<IngredientWithSize> cost;

    public final List<IngredientWithSize> getCost() {
        if (cost == null) {
            cost = makeCostList();
            Preconditions.checkNotNull(cost);
        }
        return cost;
    }

    // TODO data driven/IRecipe-based?
    @Nonnull
    protected abstract List<IngredientWithSize> makeCostList();
}
