package malte0811.controlengineering.controlpanels.scope;

import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.util.mycodec.MyCodecs;

public class AnalogModule extends ScopeModule<Unit> {
    public AnalogModule() {
        super(Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE), 1, false);
    }
}
