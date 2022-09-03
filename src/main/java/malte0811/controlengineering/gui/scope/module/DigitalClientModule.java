package malte0811.controlengineering.gui.scope.module;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.controlpanels.scope.DigitalModule.State;
import malte0811.controlengineering.controlpanels.scope.DigitalModule.TriggerState;
import malte0811.controlengineering.controlpanels.scope.ScopeModules;
import malte0811.controlengineering.gui.scope.ToggleSwitch;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DigitalClientModule extends ClientModule<State> {
    public static final String TRIGGER_LOW = ControlEngineering.MODID + ".gui.scope.digitalTriggerLow";
    public static final String TRIGGER_IGNORE = ControlEngineering.MODID + ".gui.scope.digitalTriggerIgnore";
    public static final String TRIGGER_HIGH = ControlEngineering.MODID + ".gui.scope.digitalTriggerHigh";

    public DigitalClientModule() {
        super(2, ScopeModules.DIGITAL);
    }

    @Override
    public List<ToggleSwitch> makeSwitches(Vec2i offset, State state, Consumer<State> setState) {
        List<ToggleSwitch> switches = new ArrayList<>();
        for (int i = 0; i < BusLine.LINE_SIZE; ++i) {
            final var row = i / 4;
            final var col = i % 4;
            final var iFinal = i;
            final var triggerState = state.channelTriggers().get(iFinal);
            switches.add(new ToggleSwitch(
                    translate(triggerState),
                    offset.add(16 + 21 * col, 18 + 16 * row),
                    true,
                    fromTriggerState(triggerState),
                    newState -> setState.accept(state.withTrigger(iFinal, toTriggerState(newState)))
            ));
        }
        return switches;
    }

    private static Component translate(TriggerState triggerState) {
        return Component.translatable(switch (triggerState) {
            case LOW -> TRIGGER_LOW;
            case IGNORED -> TRIGGER_IGNORE;
            case HIGH -> TRIGGER_HIGH;
        });
    }

    private static ToggleSwitch.State fromTriggerState(TriggerState state) {
        return switch (state) {
            case LOW -> ToggleSwitch.State.LOW;
            case IGNORED -> ToggleSwitch.State.NEUTRAL;
            case HIGH -> ToggleSwitch.State.HIGH;
        };
    }

    private static TriggerState toTriggerState(ToggleSwitch.State state) {
        return switch (state) {
            case LOW -> TriggerState.LOW;
            case NEUTRAL -> TriggerState.IGNORED;
            case HIGH -> TriggerState.HIGH;
        };
    }
}
