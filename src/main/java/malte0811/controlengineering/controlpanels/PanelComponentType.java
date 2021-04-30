package malte0811.controlengineering.controlpanels;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.serialization.Codecs;
import malte0811.controlengineering.util.serialization.serial.StringCodecParser;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.List;

public abstract class PanelComponentType<Config, State> extends TypedRegistryEntry<Pair<Config, State>> {
    private final Vec2i size;
    private final StringCodecParser<Config> configParser;
    private final String translationKey;

    protected PanelComponentType(
            Config defaultConfig, State intitialState,
            Codec<Config> codecConfig, Codec<State> codecState,
            Vec2i size,
            String translationKey
    ) {
        super(Pair.of(defaultConfig, intitialState), Codecs.safePair(codecConfig, codecState));
        this.size = size;
        this.configParser = StringCodecParser.getParser(codecConfig);
        this.translationKey = translationKey;
    }

    @Override
    public PanelComponentInstance<Config, State> newInstance() {
        return new PanelComponentInstance<>(this, getInitialState());
    }

    public PanelComponentInstance<Config, State> newInstance(Config config) {
        return new PanelComponentInstance<>(this, Pair.of(config, getInitialState().getSecond()));
    }

    public DataResult<PanelComponentInstance<Config, State>> newInstance(List<String> data) {
        return configParser.parse(data).map(this::newInstance);
    }

    public List<String> toCNCStrings(Config config) {
        return configParser.stringify(config);
    }

    public abstract BusState getEmittedState(Config config, State state);

    public abstract State updateTotalState(Config config, State oldState, BusState busState);

    public abstract State tick(Config config, State oldState);

    public abstract Pair<ActionResultType, State> click(Config config, State oldState);

    @Nullable
    protected abstract AxisAlignedBB createSelectionShape();

    private AxisAlignedBB selectionShape;

    @Nullable
    public AxisAlignedBB getSelectionShape() {
        if (selectionShape == null) {
            selectionShape = createSelectionShape();
        }
        return selectionShape;
    }

    public Vec2i getSize() {
        return size;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
