package malte0811.controlengineering.controlpanels;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.crafting.noncrafting.ComponentCostRecipe;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;
import malte0811.controlengineering.util.serialization.mycodec.record.RecordCodecBase;
import malte0811.controlengineering.util.serialization.serial.PacketBufferStorage;
import malte0811.controlengineering.util.serialization.serial.SerialStorage;
import malte0811.controlengineering.util.serialization.serial.StringListStorage;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public abstract class PanelComponentType<Config, State>
        extends TypedRegistryEntry<Pair<Config, State>, PanelComponentInstance<Config, State>> {
    @Nullable
    // Null = dynamic size
    private final Vec2d size;
    private final RecordCodecBase<Config> configCodec;
    private String translationKey;
    private final AABB defaultSelectionShape;

    protected PanelComponentType(
            Config defaultConfig, State intitialState,
            RecordCodecBase<Config> codecConfig, MyCodec<State> codecState,
            @Nullable Vec2d size, double selectionHeight
    ) {
        super(Pair.of(defaultConfig, intitialState), MyCodecs.pair(codecConfig, codecState));
        this.size = size;
        this.configCodec = codecConfig;

        if (selectionHeight >= 0 && size != null) {
            this.defaultSelectionShape = new AABB(0, 0, 0, size.x(), selectionHeight, size.y());
        } else {
            this.defaultSelectionShape = null;
        }
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
        return newInstance(new PacketBufferStorage(from)).orElse(null);
    }

    public FastDataResult<PanelComponentInstance<Config, State>> newInstance(List<String> data) {
        return newInstance(new StringListStorage(data));
    }

    private FastDataResult<PanelComponentInstance<Config, State>> newInstance(SerialStorage data) {
        return configCodec.fromSerial(data).map(this::newInstanceFromCfg);
    }

    public List<String> toCNCStrings(Config config) {
        StringListStorage storage = new StringListStorage();
        configCodec.toSerial(storage, config);
        return storage.getData();
    }

    public BusState getEmittedState(Config config, State state) {
        return BusState.EMPTY;
    }

    public State updateTotalState(Config config, State oldState, BusState busState) {
        return oldState;
    }

    public State tick(Config config, State oldState) {
        return oldState;
    }

    public Pair<InteractionResult, State> click(Config config, State oldState, boolean sneaking, Vec3 relativeHit) {
        return Pair.of(InteractionResult.PASS, oldState);
    }

    @Nullable
    public AABB getSelectionShape() {
        return defaultSelectionShape;
    }

    public Vec2d getSize(Config config) {
        return Objects.requireNonNull(size);
    }

    public String getTranslationKey() {
        if (translationKey == null) {
            var name = getRegistryName();
            translationKey = name.getNamespace() + ".component." + name.getPath();
        }
        return translationKey;
    }

    public boolean canClientDistinguish(State stateA, State stateB) {
        return !Objects.equals(stateA, stateB);
    }

    public final List<IngredientWithSize> getCost(Level level) {
        if (level != null) {
            var recipe = level.getRecipeManager().byKey(getCostLocation()).orElse(null);
            if (recipe instanceof ComponentCostRecipe componentCost) {
                return componentCost.getCost();
            }
        }
        return List.of(new IngredientWithSize(Ingredient.of(Items.BEDROCK)));
    }

    public final RecordCodecBase<Config> getConfigCodec() {
        return configCodec;
    }

    public ResourceLocation getCostLocation() {
        return new ResourceLocation(getRegistryName().getNamespace(), "component_cost/" + getRegistryName().getPath());
    }
}
