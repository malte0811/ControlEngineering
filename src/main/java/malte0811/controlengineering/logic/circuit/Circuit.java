package malte0811.controlengineering.logic.circuit;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.logic.cells.CircuitSignals;
import malte0811.controlengineering.logic.cells.LeafcellInstance;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec4;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Circuit {
    private static final MyCodec<List<LeafcellInstance<?, ?>>> CELLS_CODEC = MyCodecs.list(LeafcellInstance.CODEC);
    private static final MyCodec<Object2IntMap<NetReference>> NET_VALUES_CODEC =
            MyCodecs.codecForMap(NetReference.CODEC, MyCodecs.INTEGER).xmap(Object2IntOpenHashMap::new, m -> m);
    private static final MyCodec<Map<PinReference, NetReference>> PIN_NET_CODEC =
            MyCodecs.codecForMap(PinReference.CODEC, NetReference.CODEC);
    public static final MyCodec<Circuit> CODEC = new RecordCodec4<>(
            new CodecField<>("cellsInOrder", c -> c.cellsInTopoOrder, CELLS_CODEC),
            new CodecField<>("inputValues", c -> c.inputValues, NET_VALUES_CODEC),
            new CodecField<>("pinToNet", c -> c.pinToNet, PIN_NET_CODEC),
            new CodecField<>("allNetValues", c -> c.allNetValues, NET_VALUES_CODEC),
            Circuit::new
    );

    private final List<LeafcellInstance<?, ?>> cellsInTopoOrder;
    private final Object2IntMap<NetReference> allNetValues;
    private final Object2IntMap<NetReference> inputValues;
    private final Map<NetReference, PinReference> delayedNetsBySource;
    private final Map<PinReference, NetReference> pinToNet;

    public Circuit(
            List<LeafcellInstance<?, ?>> cellsInOrder,
            Set<NetReference> inputs,
            Map<PinReference, NetReference> pinNets
    ) {
        this(
                cellsInOrder,
                new Object2IntOpenHashMap<>(inputs.stream().collect(Collectors.toMap(Function.identity(), $ -> 0))),
                pinNets,
                new Object2IntOpenHashMap<>()
        );
    }

    public Circuit(
            List<LeafcellInstance<?, ?>> cellsInOrder,
            Object2IntMap<NetReference> inputs,
            Map<PinReference, NetReference> pinNets,
            Object2IntMap<NetReference> netValues
    ) {
        this.cellsInTopoOrder = cellsInOrder;
        this.pinToNet = pinNets;
        this.inputValues = inputs;
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

    public int getNetValue(NetReference net) {
        return allNetValues.getInt(net);
    }

    public void updateInputValue(NetReference net, int value) {
        Preconditions.checkArgument(inputValues.containsKey(net));
        inputValues.put(net, value);
    }

    public void tick() {
        allNetValues.clear();
        allNetValues.putAll(inputValues);
        for (Map.Entry<NetReference, PinReference> delayedNet : delayedNetsBySource.entrySet()) {
            NetReference net = delayedNet.getKey();
            PinReference pin = delayedNet.getValue();
            var cell = cellsInTopoOrder.get(pin.cell());
            allNetValues.put(
                    net, cell.getCurrentOutput(getCellInputs(pin.cell())).value(pin.pinName())
            );
        }
        for (int cellId = 0; cellId < cellsInTopoOrder.size(); cellId++) {
            var cell = cellsInTopoOrder.get(cellId);
            for (Object2IntMap.Entry<String> entry : cell.tick(getCellInputs(cellId)).entries()) {
                NetReference net = pinToNet.get(new PinReference(cellId, true, entry.getKey()));
                if (net != null) {
                    Pin pin = cell.getType().getOutputPins().get(entry.getKey());
                    if (pin.direction().isCombinatorialOutput()) {
                        Preconditions.checkState(!allNetValues.containsKey(net));
                        allNetValues.put(net, entry.getIntValue());
                    } else {
                        Preconditions.checkState(allNetValues.containsKey(net));
                    }
                }
            }
        }
    }

    private CircuitSignals getCellInputs(int cellId) {
        var cell = cellsInTopoOrder.get(cellId);
        Object2IntMap<String> inputValues = new Object2IntOpenHashMap<>();
        for (Map.Entry<String, Pin> entry : cell.getType().getInputPins().entrySet()) {
            NetReference netAtInput = pinToNet.get(new PinReference(cellId, false, entry.getKey()));
            inputValues.put(entry.getKey(), allNetValues.getInt(netAtInput));
        }
        return new CircuitSignals(inputValues);
    }
}