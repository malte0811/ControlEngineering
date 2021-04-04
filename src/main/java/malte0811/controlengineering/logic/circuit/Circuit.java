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
import java.util.stream.Stream;

public class Circuit {
    private static final String STAGES_KEY = "stages";
    private static final String NETS_KEY = "nets";
    private static final String CURRENT_VALUE_KEY = "current";
    private static final String IS_INPUT_KEY = "isInput";
    private static final String PINS_KEY = "pins";
    private static final Codec<List<List<LeafcellInstance<?>>>> CELLS_CODEC = Codec.list(Codec.list(
            LeafcellInstance.CODEC
    ));

    private final List<List<LeafcellInstance<?>>> cellsByStage;
    private final Object2DoubleMap<NetReference> allNetValues = new Object2DoubleOpenHashMap<>();
    private final Object2DoubleMap<NetReference> inputValues = new Object2DoubleOpenHashMap<>();
    private final Map<PinReference, NetReference> pinToNet;

    public Circuit(CompoundNBT nbt) {
        ListNBT stages = nbt.getList(STAGES_KEY, Constants.NBT.TAG_LIST);
        cellsByStage = Codecs.readOrNull(CELLS_CODEC, stages);
        pinToNet = new HashMap<>();
        CompoundNBT netsNBT = nbt.getCompound(NETS_KEY);
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
                if (pin != null && isValidPin(pin) && !pinToNet.containsKey(pin)) {
                    pinToNet.put(pin, netRef);
                }
            }
        }
    }

    public Circuit(
            List<List<LeafcellInstance<?>>> stages, Set<NetReference> inputs, Map<PinReference, NetReference> pinNets
    ) {
        this.cellsByStage = stages;
        this.pinToNet = pinNets;
        for (NetReference n : inputs) {
            inputValues.put(n, 0);
        }
    }

    private boolean isValidPin(PinReference pin) {
        if (pin.getStage() >= cellsByStage.size()) {
            return false;
        }
        List<LeafcellInstance<?>> stage = cellsByStage.get(pin.getStage());
        if (pin.getCellInStage() >= stage.size()) {
            return false;
        }
        LeafcellInstance<?> cell = stage.get(pin.getCellInStage());
        List<Pin> pins;
        if (pin.isOutput()) {
            pins = cell.getType().getOutputPins();
        } else {
            pins = cell.getType().getInputPins();
        }
        return pin.getPin() < pins.size();
    }

    public CompoundNBT toNBT() {
        INBT stages = Codecs.encode(CELLS_CODEC, cellsByStage);
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
        for (int stageId = 0, cellsByStageSize = cellsByStage.size(); stageId < cellsByStageSize; stageId++) {
            List<LeafcellInstance<?>> stage = cellsByStage.get(stageId);
            Object2DoubleMap<NetReference> stageOutputs = new Object2DoubleOpenHashMap<>(inputValues);
            for (int cellId = 0, stageSize = stage.size(); cellId < stageSize; cellId++) {
                LeafcellInstance<?> cell = stage.get(cellId);
                DoubleList inputValues = new DoubleArrayList();
                for (int pinId = 0; pinId < cell.getType().getInputPins().size(); pinId++) {
                    NetReference netAtInput = pinToNet.get(new PinReference(stageId, cellId, false, pinId));
                    inputValues.add(allNetValues.getDouble(netAtInput));
                }
                DoubleList outputValues = cell.tick(inputValues);
                for (int pinId = 0; pinId < outputValues.size(); pinId++) {
                    NetReference net = pinToNet.get(new PinReference(stageId, cellId, true, pinId));
                    if (net != null) {
                        Preconditions.checkState(!allNetValues.containsKey(net) && !stageOutputs.containsKey(net));
                        stageOutputs.put(net, outputValues.getDouble(pinId));
                    }
                }
            }
            allNetValues.putAll(stageOutputs);
        }
    }

    public Object2DoubleMap<NetReference> getNetValues() {
        return Object2DoubleMaps.unmodifiable(allNetValues);
    }

    public Collection<NetReference> getInputNets() {
        return inputValues.keySet();
    }

    public Stream<LeafcellType<?>> getCellTypes() {
        return cellsByStage.stream().flatMap(List::stream).map(LeafcellInstance::getType);
    }
}
