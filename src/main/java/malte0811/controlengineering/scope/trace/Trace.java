package malte0811.controlengineering.scope.trace;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class Trace {
    public static final MyCodec<Trace> CODEC = new RecordCodec2<>(
            MyCodecs.list(MyCodecs.DOUBLE).fieldOf("samples", Trace::getDivRelativeSamples),
            TraceId.CODEC.fieldOf("traceId", t -> t.traceId),
            Trace::new
    );

    private final DoubleList samples;
    private final TraceId traceId;

    public Trace(TraceId traceId) {
        this(List.of(), traceId);
    }

    private Trace(List<Double> samples, TraceId traceId) {
        this.samples = new DoubleArrayList(samples);
        this.traceId = traceId;
    }

    public Trace(Trace oldTrace) {
        this(oldTrace.samples, oldTrace.traceId);
    }

    public boolean isValid(List<ModuleInScope> modules) {
        return getMaybeOwner(modules) != null;
    }

    public double addSample(List<ModuleInScope> modules, BusState input) {
        final var owner = getMaybeOwner(modules);
        if (owner != null) {
            final var sample = owner.module().getDivRelativeSample(traceId.traceIdWithinModule(), input);
            addSample(sample);
            return sample;
        } else {
            // TODO stop people from taking modules from a running scope
            addSample(0);
            return 0;
        }
    }

    public void addSample(double sample) {
        this.samples.add(sample);
    }

    public DoubleList getDivRelativeSamples() {
        return samples;
    }

    private ModuleInScope getOwner(List<ModuleInScope> modules) {
        return Objects.requireNonNull(getMaybeOwner(modules));
    }

    public DoubleList getSamples() {
        return DoubleLists.unmodifiable(samples);
    }

    public TraceId getTraceId() {
        return traceId;
    }

    @Nullable
    private ModuleInScope getMaybeOwner(List<ModuleInScope> modules) {
        for (final var module : modules) {
            if (module.firstSlot() == traceId.firstSlotOfModule()) {
                if (traceId.traceIdWithinModule() < module.type().getNumTraces()) {
                    return module;
                } else {
                    return null;
                }
            }
        }
        return null;
    }
}
