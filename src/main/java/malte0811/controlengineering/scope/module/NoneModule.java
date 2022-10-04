package malte0811.controlengineering.scope.module;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import javax.annotation.Nullable;

public class NoneModule extends ScopeModule<Unit> {
    public NoneModule() {
        super(Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE), 1, true);
    }

    @Nullable
    @Override
    public Unit enableSomeTrigger(Unit withoutTrigger) {
        return null;
    }

    @Override
    public Unit disableTrigger(Unit withTrigger) {
        return withTrigger;
    }

    @Override
    public boolean isSomeTriggerEnabled(Unit unit) {
        return false;
    }

    @Override
    public Pair<Boolean, Unit> isTriggered(Unit oldState, BusState input) {
        return Pair.of(false, oldState);
    }

    @Override
    public IntList getActiveTraces(Unit unit) {
        return IntLists.emptyList();
    }

    @Override
    public int getNumTraces() {
        return 0;
    }

    @Override
    public int getModulePowerConsumption(Unit unit) {
        return 0;
    }

    @Override
    public double getTraceValueInDivs(int traceId, BusState input, Unit currentState) {
        return 0;
    }

    @Override
    public boolean isEnabled(Unit unit) {
        return false;
    }
}
