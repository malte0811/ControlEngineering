package malte0811.controlengineering.logic.circuit;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import malte0811.controlengineering.logic.cells.LeafcellInstance;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Circuit {
    private static final String STAGES_KEY = "stages";
    private static final String NETS_KEY = "nets";
    private static final String CURRENT_VALUE_KEY = "current";
    private static final String IS_INPUT_KEY = "isInput";
    private static final String PINS_KEY = "pins";
    private static final MyCodec<List<LeafcellInstance<?>>> CELLS_CODEC = MyCodecs.list(LeafcellInstance.CODEC);

    private final List<LeafcellInstance<?>> cellsInTopoOrder;
    private final Object2DoubleMap<NetReference> allNetValues;
    private final Object2DoubleMap<NetReference> inputValues;
    private final Map<NetReference, PinReference> delayedNetsBySource;
    private final Map<PinReference, NetReference> pinToNet;

    public static Circuit fromNBT(CompoundTag nbt) {
        ListTag stages = nbt.getList(STAGES_KEY, Tag.TAG_LIST);
        List<LeafcellInstance<?>> cellsInTopoOrder = CELLS_CODEC.fromNBT(stages);
        Map<PinReference, NetReference> pinToNet = new HashMap<>();
        CompoundTag netsNBT = nbt.getCompound(NETS_KEY);
        Object2DoubleMap<NetReference> allNetValues = new Object2DoubleOpenHashMap<>();
        Object2DoubleMap<NetReference> inputValues = new Object2DoubleOpenHashMap<>();
        for (String netName : netsNBT.getAllKeys()) {
            CompoundTag netNBT = netsNBT.getCompound(netName);
            NetReference netRef = new NetReference(netName);
            final double value = netNBT.getDouble(CURRENT_VALUE_KEY);
            allNetValues.put(netRef, value);
            if (netNBT.getBoolean(IS_INPUT_KEY)) {
                inputValues.put(netRef, value);
            }
            for (Tag pinNBT : netNBT.getList(PINS_KEY, Tag.TAG_COMPOUND)) {
                PinReference pin = PinReference.CODEC.fromNBT(pinNBT);
                if (pin != null && isValidPin(pin, cellsInTopoOrder) && !pinToNet.containsKey(pin)) {
                    pinToNet.put(pin, netRef);
                }
            }
        }
        return new Circuit(cellsInTopoOrder, inputValues, pinToNet, allNetValues);
    }

    public Circuit(
            List<LeafcellInstance<?>> cellsInOrder,
            Set<NetReference> inputs,
            Map<PinReference, NetReference> pinNets
    ) {
        this(
                cellsInOrder,
                inputs.stream().collect(Collectors.toMap(Function.identity(), $ -> 0.)),
                pinNets,
                new Object2DoubleOpenHashMap<>()
        );
    }

    public Circuit(
            List<LeafcellInstance<?>> cellsInOrder,
            Map<NetReference, Double> inputs,
            Map<PinReference, NetReference> pinNets,
            Object2DoubleMap<NetReference> netValues
    ) {
        this.cellsInTopoOrder = cellsInOrder;
        this.pinToNet = pinNets;
        this.inputValues = new Object2DoubleOpenHashMap<>(inputs);
        Map<NetReference, PinReference> delayedSources = new HashMap<>();
        for (Map.Entry<PinReference, NetReference> entry : pinNets.entrySet()) {
            if (!entry.getKey().isOutput()) {
                continue;
            }
            Map<String, Pin> pins = cellsInOrder.get(entry.getKey().cell()).getType().getOutputPins();
            if (!pins.get(entry.getKey().pinName()).direction().isCombinatorialOutput()) {
                delayedSources.put(entry.getValue(), entry.getKey());
            }
        }
        this.delayedNetsBySource = delayedSources;
        this.allNetValues = netValues;
    }

    private static boolean isValidPin(PinReference pin, List<LeafcellInstance<?>> cellsInTopoOrder) {
        if (pin.cell() >= cellsInTopoOrder.size()) {
            return false;
        }
        LeafcellInstance<?> cell = cellsInTopoOrder.get(pin.cell());
        Map<String, Pin> pins;
        if (pin.isOutput()) {
            pins = cell.getType().getOutputPins();
        } else {
            pins = cell.getType().getInputPins();
        }
        return pins.containsKey(pin.pinName());
    }

    public CompoundTag toNBT() {
        Tag stages = CELLS_CODEC.toNBT(cellsInTopoOrder);
        Map<NetReference, List<PinReference>> pinsByNet = new HashMap<>();
        for (Map.Entry<PinReference, NetReference> entry : pinToNet.entrySet()) {
            pinsByNet.computeIfAbsent(entry.getValue(), $ -> new ArrayList<>()).add(entry.getKey());
        }
        CompoundTag nets = new CompoundTag();
        for (Map.Entry<NetReference, List<PinReference>> entry : pinsByNet.entrySet()) {
            NetReference net = entry.getKey();
            CompoundTag netNBT = new CompoundTag();
            netNBT.putDouble(CURRENT_VALUE_KEY, allNetValues.getDouble(net));
            ListTag netPins = new ListTag();
            for (PinReference pin : entry.getValue()) {
                netPins.add(PinReference.CODEC.toNBT(pin));
            }
            netNBT.put(PINS_KEY, netPins);
            netNBT.putBoolean(IS_INPUT_KEY, inputValues.containsKey(net));
            nets.put(net.id(), netNBT);
        }
        CompoundTag result = new CompoundTag();
        result.put(STAGES_KEY, stages);
        result.put(NETS_KEY, nets);
        return result;
    }

    public double getNetValue(NetReference net) {
        return allNetValues.getDouble(net);
    }

    public void updateInputValue(NetReference net, double value) {
        Preconditions.checkArgument(inputValues.containsKey(net));
        inputValues.put(net, value);
    }

    public void tick() {
        allNetValues.clear();
        allNetValues.putAll(inputValues);
        for (Map.Entry<NetReference, PinReference> delayedNet : delayedNetsBySource.entrySet()) {
            NetReference net = delayedNet.getKey();
            PinReference pin = delayedNet.getValue();
            LeafcellInstance<?> cell = cellsInTopoOrder.get(pin.cell());
            allNetValues.put(
                    net, cell.getCurrentOutput(getCellInputs(pin.cell())).getDouble(pin.pinName())
            );
        }
        for (int cellId = 0; cellId < cellsInTopoOrder.size(); cellId++) {
            LeafcellInstance<?> cell = cellsInTopoOrder.get(cellId);
            for (Object2DoubleMap.Entry<String> entry : cell.tick(getCellInputs(cellId)).object2DoubleEntrySet()) {
                NetReference net = pinToNet.get(new PinReference(cellId, true, entry.getKey()));
                if (net != null) {
                    Pin pin = cell.getType().getOutputPins().get(entry.getKey());
                    if (pin.direction().isCombinatorialOutput()) {
                        Preconditions.checkState(!allNetValues.containsKey(net));
                        allNetValues.put(net, entry.getDoubleValue());
                    } else {
                        Preconditions.checkState(allNetValues.containsKey(net));
                    }
                }
            }
        }
    }

    private Object2DoubleMap<String> getCellInputs(int cellId) {
        LeafcellInstance<?> cell = cellsInTopoOrder.get(cellId);
        Object2DoubleMap<String> inputValues = new Object2DoubleArrayMap<>();
        for (Map.Entry<String, Pin> entry : cell.getType().getInputPins().entrySet()) {
            NetReference netAtInput = pinToNet.get(new PinReference(cellId, false, entry.getKey()));
            inputValues.put(entry.getKey(), allNetValues.getDouble(netAtInput));
        }
        return inputValues;
    }

    public Object2DoubleMap<NetReference> getNetValues() {
        return Object2DoubleMaps.unmodifiable(allNetValues);
    }

    public Collection<NetReference> getInputNets() {
        return inputValues.keySet();
    }

    public Stream<LeafcellType<?>> getCellTypes() {
        return cellsInTopoOrder.stream().map(LeafcellInstance::getType);
    }
}
