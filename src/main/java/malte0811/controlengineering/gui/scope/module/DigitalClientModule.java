package malte0811.controlengineering.gui.scope.module;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.gui.scope.components.LogicConnector;
import malte0811.controlengineering.gui.scope.components.Range;
import malte0811.controlengineering.gui.scope.components.ScopeButton;
import malte0811.controlengineering.gui.scope.components.ToggleSwitch;
import malte0811.controlengineering.scope.module.DigitalModule;
import malte0811.controlengineering.scope.module.DigitalModule.State;
import malte0811.controlengineering.scope.module.DigitalModule.TriggerState;
import malte0811.controlengineering.scope.module.ScopeModules;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DigitalClientModule extends ClientModule<State> {
    public static final String TRIGGER_LOW = ControlEngineering.MODID + ".gui.scope.digitalTriggerLow";
    public static final String TRIGGER_IGNORE = ControlEngineering.MODID + ".gui.scope.digitalTriggerIgnore";
    public static final String TRIGGER_HIGH = ControlEngineering.MODID + ".gui.scope.digitalTriggerHigh";
    public static final String INPUT_OPEN = ControlEngineering.MODID + ".gui.scope.logicInputOpen";
    public static final String INPUT_CONNECTED = ControlEngineering.MODID + ".gui.scope.logicConnected";

    public DigitalClientModule() {
        super(2, ScopeModules.DIGITAL);
    }

    @Override
    public List<PoweredComponent> createComponents(
            Vec2i offset, State state, Consumer<State> setState, boolean scopePowered
    ) {
        final var inputState = state.inputState();
        List<PoweredComponent> switches = new ArrayList<>();
        switches.add(ScopeButton.makeModuleEnable(
                offset.add(16, 3), state.moduleEnabled(), () -> setState.accept(state.toggleModule())
        ).powered(scopePowered));
        final var modulePowered = scopePowered && state.moduleEnabled();
        switches.add(ScopeButton.makeTriggerEnable(
                offset.add(22, 3), inputState.triggerEnabled(), () -> setState.accept(state.withTrigger(true))
        ).powered(modulePowered));
        switches.add(Range.makeVerticalOffset(
                offset.add(40, 3), state.verticalOffset(), i -> setState.accept(state.withOffset(i))
        ).powered(modulePowered));
        final Component connectorTooltip;
        if (inputState.inputLine() == DigitalModule.NO_LINE) {
            connectorTooltip = Component.translatable(INPUT_OPEN);
        } else {
            connectorTooltip = Component.translatable(INPUT_CONNECTED, inputState.inputLine());
        }
        switches.add(new LogicConnector(
                offset.add(40, 80), inputState.inputLine(), connectorTooltip, i -> setState.accept(state.withInput(i))
        ).powered(modulePowered));
        for (int i = 0; i < BusLine.LINE_SIZE; ++i) {
            final var iFinal = i;
            final var triggerState = inputState.channelTriggers().get(iFinal);
            final var channelBasePos = offset.add(getChannelOffset(i));
            switches.add(new ToggleSwitch(
                    translate(triggerState),
                    channelBasePos.add(16, 18),
                    true,
                    fromTriggerState(triggerState),
                    newState -> setState.accept(state.withTrigger(iFinal, toTriggerState(newState)))
            ).powered(modulePowered));
            final var channelActive = inputState.isChannelVisible(i);
            switches.add(ScopeButton.makeChannelEnable(
                    channelBasePos.add(22, 22),
                    channelActive,
                    () -> setState.accept(state.toggleChannel(iFinal))
            ).powered(modulePowered));
        }
        return switches;
    }

    @Override
    protected List<RectangleI> computeRelativeChannelAreas() {
        List<RectangleI> areas = new ArrayList<>(BusLine.LINE_SIZE);
        final var baseRect = new RectangleI(11, 18, 25, 29);
        for (int i = 0; i < BusLine.LINE_SIZE; ++i) {
            areas.add(baseRect.offset(getChannelOffset(i)));
        }
        return areas;
    }

    private static Vec2i getChannelOffset(int channelId) {
        final var row = channelId / 4;
        final var col = channelId % 4;
        return new Vec2i(21 * col, 16 * row);
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
