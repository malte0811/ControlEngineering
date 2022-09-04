package malte0811.controlengineering.controlpanels.scope;

import com.mojang.datafixers.util.Unit;
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
}
