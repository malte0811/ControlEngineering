package malte0811.controlengineering.controlpanels.scope;

import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.util.mycodec.MyCodecs;

public class NoneModule extends ScopeModule<Unit> {
    public NoneModule() {
        super(Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE));
    }

    @Override
    public int getWidth() {
        return 1;
    }
}
