package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.scope.trace.Trace;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import javax.annotation.Nullable;
import java.util.List;

public record AddTraceSamples(List<Double> samples) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<AddTraceSamples> CODEC = MyCodecs.list(MyCodecs.DOUBLE).xmap(
            AddTraceSamples::new, AddTraceSamples::samples
    );

    @Override
    public boolean process(List<ScopeBlockEntity.ModuleInScope> modules, @Nullable List<Trace> traces) {
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
