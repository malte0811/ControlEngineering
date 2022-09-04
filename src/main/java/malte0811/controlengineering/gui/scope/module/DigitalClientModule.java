package malte0811.controlengineering.gui.scope.module;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.controlpanels.scope.DigitalModule.State;
import malte0811.controlengineering.controlpanels.scope.DigitalModule.TriggerState;
import malte0811.controlengineering.controlpanels.scope.ScopeModules;
import malte0811.controlengineering.gui.scope.components.IScopeComponent;
import malte0811.controlengineering.gui.scope.components.ScopeButton;
import malte0811.controlengineering.gui.scope.components.ToggleSwitch;
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
    public List<IScopeComponent> createComponents(Vec2i offset, State state, Consumer<State> setState) {
        List<IScopeComponent> switches = new ArrayList<>();
        switches.add(ScopeButton.makeModuleEnable(
                offset.add(16, 3), state.moduleEnabled(), () -> setState.accept(state.toggleModule())
        ));
        switches.add(ScopeButton.makeTriggerEnable(
                offset.add(22, 3), state.triggerEnabled(), () -> setState.accept(state.withTrigger(true))
        ));
        for (int i = 0; i < BusLine.LINE_SIZE; ++i) {
            final var row = i / 4;
            final var col = i % 4;
            final var iFinal = i;
            final var triggerState = state.channelTriggers().get(iFinal);
            final var channelBasePos = offset.add(21 * col, 16 * row);
            switches.add(new ToggleSwitch(
                    translate(triggerState),
                    channelBasePos.add(16, 18),
                    true,
                    fromTriggerState(triggerState),
                    newState -> setState.accept(state.withTrigger(iFinal, toTriggerState(newState)))
            ));
            final var channelActive = state.isChannelVisible(i);
            switches.add(ScopeButton.makeChannelEnable(
                    channelBasePos.add(22, 22),
                    channelActive,
                    () -> setState.accept(state.toggleChannel(iFinal))
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
