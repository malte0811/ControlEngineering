package malte0811.controlengineering.gui.scope.module;

import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.scope.module.ScopeModules;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2i;

import java.util.List;
import java.util.function.Consumer;

public class NoneClientModule extends ClientModule<Unit> {
    public NoneClientModule() {
        super(0, ScopeModules.NONE);
    }

    @Override
    public List<PoweredComponent> createComponents(
            Vec2i offset, Unit state, Consumer<Unit> setState,
            boolean scopePowered
    ) {
        return List.of();
    }

    @Override
    protected List<RectangleI> computeRelativeChannelAreas() {
        return List.of();
    }
}
