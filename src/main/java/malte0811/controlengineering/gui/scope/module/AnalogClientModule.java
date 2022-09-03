package malte0811.controlengineering.gui.scope.module;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.scope.AnalogModule.State;
import malte0811.controlengineering.controlpanels.scope.ScopeModules;
import malte0811.controlengineering.gui.scope.ToggleSwitch;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class AnalogClientModule extends ClientModule<State> {
    public static final String TRIGGER_POLARITY_TOOLTIP = ControlEngineering.MODID + ".gui.scope.analogTriggerPolarity";

    public AnalogClientModule() {
        super(1, ScopeModules.ANALOG);
    }

    @Override
    public List<ToggleSwitch> makeSwitches(Vec2i offset, State state, Consumer<State> setState) {
        return List.of(new ToggleSwitch(
                Component.translatable(TRIGGER_POLARITY_TOOLTIP),
                offset.add(4, 14),
                state.risingTrigger(),
                b -> setState.accept(new State(b))
        ));
    }
}
