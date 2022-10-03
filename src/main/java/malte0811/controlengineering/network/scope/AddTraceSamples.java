package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.GlobalState;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import org.apache.commons.lang3.mutable.Mutable;

import java.util.List;

public record AddTraceSamples(List<Double> samples) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<AddTraceSamples> CODEC = MyCodecs.list(MyCodecs.DOUBLE).xmap(
            AddTraceSamples::new, AddTraceSamples::samples
    );

    @Override
    public boolean process(
            List<ScopeBlockEntity.ModuleInScope> modules,
            Mutable<Traces> tracesM,
            Mutable<GlobalConfig> globalConfig,
            Mutable<GlobalState> globalState
    ) {
        final var traces = tracesM.getValue().traces();
        if (traces == null || samples.size() != traces.size()) {
            return false;
        }
        for (int i = 0; i < traces.size(); ++i) {
            traces.get(i).addSample(samples.get(i));
        }
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
