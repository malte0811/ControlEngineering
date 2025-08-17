package malte0811.controlengineering.scope.trace;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
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
    // Not synced/saved, only used for rendering on the client
    private final LongList sampleTimestamps;
    private final TraceId traceId;

    public Trace(TraceId traceId) {
        this(List.of(), traceId);
    }

    private Trace(List<Double> samples, TraceId traceId) {
        this.samples = new DoubleArrayList(samples);
        this.traceId = traceId;
        this.sampleTimestamps = new LongArrayList(new long[this.samples.size()]);
    }

    public Trace(Trace oldTrace) {
        this(oldTrace.samples, oldTrace.traceId);
    }

    public double addSample(List<ModuleInScope> modules, BusState input) {
        final var sample = getOwner(modules).module().getDivRelativeSample(traceId.traceIdWithinModule(), input);
        addSample(sample);
        return sample;
    }

    public void addSample(double sample) {
        this.samples.add(sample);
        this.sampleTimestamps.add(System.currentTimeMillis());
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

    public LongList getSampleTimestamps() {
        return sampleTimestamps;
    }
}
