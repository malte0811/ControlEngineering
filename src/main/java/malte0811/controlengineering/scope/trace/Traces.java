package malte0811.controlengineering.scope.trace;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.network.scope.AddTraceSamples;
import malte0811.controlengineering.network.scope.ScopeSubPacket.IScopeSubPacket;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.NUM_HORIZONTAL_DIVS;

public record Traces(List<Trace> traces, int ticksPerDiv) {
    public static final MyCodec<Traces> CODEC = new RecordCodec2<>(
            MyCodecs.list(Trace.CODEC).fieldOf("traces", Traces::traces),
            MyCodecs.INTEGER.fieldOf("ticksPerDiv", Traces::ticksPerDiv),
            Traces::new
    );

    public Traces() {
        this(List.of(), 16);
    }

    @Nullable
    public IScopeSubPacket collectSample(List<ModuleInScope> modules, BusState currentBusState) {
        if (!isSweeping()) {
            return null;
        }
        List<Double> addedSamples = new ArrayList<>(traces.size());
        for (Trace next : traces) {
            addedSamples.add(next.addSample(modules, currentBusState));
        }
        return new AddTraceSamples(addedSamples);
    }

    public boolean isSweeping() {
        return !traces.isEmpty() && traces.get(0).getSamples().size() <= ticksPerDiv * NUM_HORIZONTAL_DIVS;
    }
}
