package malte0811.controlengineering.gui.scope.module;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.scope.AnalogModule;
import malte0811.controlengineering.controlpanels.scope.AnalogModule.State;
import malte0811.controlengineering.controlpanels.scope.ScopeModules;
import malte0811.controlengineering.gui.scope.components.IScopeComponent;
import malte0811.controlengineering.gui.scope.components.Range;
import malte0811.controlengineering.gui.scope.components.ScopeButton;
import malte0811.controlengineering.gui.scope.components.ToggleSwitch;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AnalogClientModule extends ClientModule<State> {
    public static final String TRIGGER_POLARITY_TOOLTIP = ControlEngineering.MODID + ".gui.scope.analogTriggerPolarity";
    public static final String PER_DIV_TOOLTIP = ControlEngineering.MODID + ".gui.scope.perDiv";
    public static final String TRIGGER_LEVEL_TOOLTIP = ControlEngineering.MODID + ".gui.scope.triggerLevel";

    public AnalogClientModule() {
        super(1, ScopeModules.ANALOG);
    }

    @Override
    public List<IScopeComponent> createComponents(Vec2i offset, State state, Consumer<State> setState) {
        List<IScopeComponent> components = new ArrayList<>();
        components.add(new ToggleSwitch(
                Component.translatable(TRIGGER_POLARITY_TOOLTIP),
                offset.add(4, 14),
                state.trigger().risingSlope(),
                b -> setState.accept(state.withTriggerSlope(b))
        ));
        final var enabled = state.moduleEnabled();
        components.add(ScopeButton.makeModuleEnable(
                offset.add(17, 3), enabled, () -> setState.accept(state.setEnabled(!enabled))
        ));
        components.add(Range.makeLinear(
                Component.translatable(TRIGGER_LEVEL_TOOLTIP),
                offset.add(24, 12),
                0, 255, 1, 10, state.trigger().level(),
                i -> setState.accept(state.withTriggerLevel(i))
        ));
        createChannelComponent(AnalogModule.TriggerChannel.LEFT, offset.add(0, 28), components, state, setState);
        createChannelComponent(AnalogModule.TriggerChannel.RIGHT, offset.add(24, 28), components, state, setState);
        return components;
    }

    private void createChannelComponent(
            AnalogModule.TriggerChannel channel,
            Vec2i baseOffset,
            List<IScopeComponent> out,
            State state,
            Consumer<State> setState
    ) {
        final var channelState = state.getChannel(channel);
        final var isEnabled = channelState.enabled();
        out.add(ScopeButton.makeChannelEnable(
                baseOffset.add(13, 2), isEnabled, () -> setState.accept(state.setChannelEnabled(channel, !isEnabled))
        ));
        out.add(ScopeButton.makeTriggerEnable(
                baseOffset.add(9, 2),
                state.trigger().source() == channel,
                () -> setState.accept(state.withTriggerChannel(channel))
        ));
        out.add(Range.makeExponential(
                Component.translatable(PER_DIV_TOOLTIP),
                baseOffset.add(2, 29),
                1, 255, 10, channelState.perDiv(),
                i -> setState.accept(state.setPerDiv(channel, i))
        ));
        out.add(Range.makeVerticalOffset(
                baseOffset.add(2, 8), channelState.zeroOffsetPixels(), i -> setState.accept(state.setOffset(channel, i))
        ));
    }
}
