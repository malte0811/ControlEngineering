package malte0811.controlengineering.itemdata;

import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class CEDataComponents {
    public static final DeferredRegister<DataComponentType<?>> REGISTER = DeferredRegister.create(
            Registries.DATA_COMPONENT_TYPE, ControlEngineering.MODID
    );

    public static final Supplier<DataComponentType<UUID>> LOCK_ID = make("lock", MyCodecs.UUID_CODEC);
    public static final Supplier<DataComponentType<List<PlacedComponent>>> PANEL_COMPONENTS = make("panel_components", MyCodecs.list(PlacedComponent.CODEC));
    public static final Supplier<DataComponentType<PanelTransform.BETransformData>> PANEL_TRANSFORM = make("panel_transform", PanelTransform.CODEC);

    public static final Supplier<DataComponentType<Schematic>> SCHEMATIC = make("schematic", Schematic.CODEC);

    public static final Supplier<DataComponentType<Integer>> EMPTY_TAPE_LENGTH = make("empty_tape_length", MyCodecs.INTEGER);
    public static final Supplier<DataComponentType<ByteList>> TAPE_CONTENT = make("tape_content", MyCodecs.BYTE_LIST);

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> make(String name, MyCodec<T> codec)
    {
        return REGISTER.register(
                name, () -> DataComponentType.<T>builder()
                        .persistent(codec.toDFUCodec())
                        .networkSynchronized(codec.streamCodec())
                        .build()
        );
    }
}
