package malte0811.controlengineering.controlpanels.scope;

import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.util.mycodec.MyCodecs;

public class DigitalModule extends ScopeModule<Unit> {
    public DigitalModule() {
        super(Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE), 2, false);
    }
}
