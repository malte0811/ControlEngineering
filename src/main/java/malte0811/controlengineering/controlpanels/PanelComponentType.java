package malte0811.controlengineering.controlpanels;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.serialization.Codecs;
import malte0811.controlengineering.util.serialization.serial.SerialCodecParser;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class PanelComponentType<Config, State> extends TypedRegistryEntry<Pair<Config, State>> {
    private final Vec2i size;
    private final SerialCodecParser<Config> configParser;
    private final String translationKey;

    protected PanelComponentType(
            Config defaultConfig, State intitialState,
            Codec<Config> codecConfig, Codec<State> codecState,
            Vec2i size,
            String translationKey
    ) {
        super(Pair.of(defaultConfig, intitialState), Codecs.safePair(codecConfig, codecState));
        this.size = size;
        this.configParser = SerialCodecParser.getParser(codecConfig);
        this.translationKey = translationKey;
    }

    @Override
    public PanelComponentInstance<Config, State> newInstance() {
        return new PanelComponentInstance<>(this, getInitialState());
    }

    public PanelComponentInstance<Config, State> newInstance(Config config) {
        return new PanelComponentInstance<>(this, Pair.of(config, getInitialState().getSecond()));
    }

    @Nullable
    public PanelComponentInstance<Config, State> newInstance(PacketBuffer from) {
        return configParser.parse(from).map(this::newInstance).result().orElse(null);
    }

    public DataResult<PanelComponentInstance<Config, State>> newInstance(List<String> data) {
        return configParser.parse(data).map(this::newInstance);
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

    public abstract Pair<ActionResultType, State> click(Config config, State oldState);

    protected abstract double getSelectionHeight();

    private final Lazy<AxisAlignedBB> defaultSelectionShape = Lazy.of(() -> {
        final double height = getSelectionHeight();
        if (height >= 0) {
            final Vec2i size = getSize(getInitialState().getFirst());
            return new AxisAlignedBB(0, 0, 0, size.x, height, size.y);
        } else {
            return null;
        }
    });

    @Nullable
    public AxisAlignedBB getSelectionShape() {
        return defaultSelectionShape.get();
    }

    public Vec2i getSize(Config config) {
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
