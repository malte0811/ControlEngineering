package malte0811.controlengineering.logic.circuit;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import malte0811.controlengineering.logic.cells.LeafcellInstance;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

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
    private static final Codec<List<LeafcellInstance<?>>> CELLS_CODEC = Codec.list(LeafcellInstance.CODEC);

    private final List<LeafcellInstance<?>> cellsInTopoOrder;
    private final Object2DoubleMap<NetReference> allNetValues;
    private final Object2DoubleMap<NetReference> inputValues;
    private final Map<NetReference, PinReference> delayedNetsBySource;
    private final Map<PinReference, NetReference> pinToNet;

    public static Circuit fromNBT(CompoundNBT nbt) {
        ListNBT stages = nbt.getList(STAGES_KEY, Constants.NBT.TAG_LIST);
        List<LeafcellInstance<?>> cellsInTopoOrder = Codecs.readOrNull(CELLS_CODEC, stages);
        Map<PinReference, NetReference> pinToNet = new HashMap<>();
        CompoundNBT netsNBT = nbt.getCompound(NETS_KEY);
        Object2DoubleMap<NetReference> allNetValues = new Object2DoubleOpenHashMap<>();
        Object2DoubleMap<NetReference> inputValues = new Object2DoubleOpenHashMap<>();
        for (String netName : netsNBT.keySet()) {
            CompoundNBT netNBT = netsNBT.getCompound(netName);
            NetReference netRef = new NetReference(netName);
            final double value = netNBT.getDouble(CURRENT_VALUE_KEY);
            allNetValues.put(netRef, value);
            if (netNBT.getBoolean(IS_INPUT_KEY)) {
                inputValues.put(netRef, value);
            }
            for (INBT pinNBT : netNBT.getList(PINS_KEY, Constants.NBT.TAG_COMPOUND)) {
                PinReference pin = Codecs.readOrNull(PinReference.CODEC, pinNBT);
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
            List<Pin> pins = cellsInOrder.get(entry.getKey().getCell()).getType().getOutputPins();
            if (!pins.get(entry.getKey().getPin()).getDirection().isCombinatorialOutput()) {
                delayedSources.put(entry.getValue(), entry.getKey());
            }
        }
        this.delayedNetsBySource = delayedSources;
        this.allNetValues = netValues;
    }

    private static boolean isValidPin(PinReference pin, List<LeafcellInstance<?>> cellsInTopoOrder) {
        if (pin.getCell() >= cellsInTopoOrder.size()) {
            return false;
        }
        LeafcellInstance<?> cell = cellsInTopoOrder.get(pin.getCell());
        List<Pin> pins;
        if (pin.isOutput()) {
            pins = cell.getType().getOutputPins();
        } else {
            pins = cell.getType().getInputPins();
        }
        return pin.getPin() < pins.size();
    }

    public CompoundNBT toNBT() {
        INBT stages = Codecs.encode(CELLS_CODEC, cellsInTopoOrder);
        Map<NetReference, List<PinReference>> pinsByNet = new HashMap<>();
        for (Map.Entry<PinReference, NetReference> entry : pinToNet.entrySet()) {
            pinsByNet.computeIfAbsent(entry.getValue(), $ -> new ArrayList<>()).add(entry.getKey());
        }
        CompoundNBT nets = new CompoundNBT();
        for (Map.Entry<NetReference, List<PinReference>> entry : pinsByNet.entrySet()) {
            NetReference net = entry.getKey();
            CompoundNBT netNBT = new CompoundNBT();
            netNBT.putDouble(CURRENT_VALUE_KEY, allNetValues.getDouble(net));
            ListNBT netPins = new ListNBT();
            for (PinReference pin : entry.getValue()) {
                netPins.add(Codecs.encode(PinReference.CODEC, pin));
            }
            netNBT.put(PINS_KEY, netPins);
            netNBT.putBoolean(IS_INPUT_KEY, inputValues.containsKey(net));
            nets.put(net.getId(), netNBT);
        }
        CompoundNBT result = new CompoundNBT();
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
            LeafcellInstance<?> cell = cellsInTopoOrder.get(pin.getCell());
            allNetValues.put(
                    net, cell.getCurrentOutput(getCellInputs(pin.getCell())).getDouble(pin.getPin())
            );
        }
        for (int cellId = 0; cellId < cellsInTopoOrder.size(); cellId++) {
            LeafcellInstance<?> cell = cellsInTopoOrder.get(cellId);
            DoubleList outputValues = cell.tick(getCellInputs(cellId));
            for (int pinId = 0; pinId < outputValues.size(); pinId++) {
                NetReference net = pinToNet.get(new PinReference(cellId, true, pinId));
                if (net != null) {
                    if (cell.getType().getOutputPins().get(pinId).getDirection().isCombinatorialOutput()) {
                        Preconditions.checkState(!allNetValues.containsKey(net));
                        allNetValues.put(net, outputValues.getDouble(pinId));
                    } else {
                        Preconditions.checkState(allNetValues.containsKey(net));
                    }
                }
            }
        }
    }

    private DoubleList getCellInputs(int cellId) {
        LeafcellInstance<?> cell = cellsInTopoOrder.get(cellId);
        DoubleList inputValues = new DoubleArrayList();
        for (int pinId = 0; pinId < cell.getType().getInputPins().size(); pinId++) {
            NetReference netAtInput = pinToNet.get(new PinReference(cellId, false, pinId));
            inputValues.add(allNetValues.getDouble(netAtInput));
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
