package malte0811.controlengineering.gui.scope.module;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.scope.components.BNCConnector;
import malte0811.controlengineering.gui.scope.components.Range;
import malte0811.controlengineering.gui.scope.components.ScopeButton;
import malte0811.controlengineering.gui.scope.components.ToggleSwitch;
import malte0811.controlengineering.scope.module.AnalogModule;
import malte0811.controlengineering.scope.module.AnalogModule.State;
import malte0811.controlengineering.scope.module.ScopeModules;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class AnalogClientModule extends ClientModule<State> {
    public static final String TRIGGER_POLARITY_TOOLTIP = ControlEngineering.MODID + ".gui.scope.analogTriggerPolarity";
    public static final String PER_DIV_TOOLTIP = ControlEngineering.MODID + ".gui.scope.perDiv";
    public static final String TRIGGER_LEVEL_TOOLTIP = ControlEngineering.MODID + ".gui.scope.triggerLevel";
    public static final String BNC_OPEN = ControlEngineering.MODID + ".gui.scope.bncOpen";
    public static final String BNC_CONNECTED = ControlEngineering.MODID + ".gui.scope.bncConnected";

    public AnalogClientModule() {
        super(1, ScopeModules.ANALOG);
    }

    @Override
    public List<PoweredComponent> createComponents(
            Vec2i offset, State state, Consumer<State> setState,
            boolean scopePowered
    ) {
        final var moduleEnabled = scopePowered && state.moduleEnabled();
        List<PoweredComponent> components = new ArrayList<>();
        components.add(new ToggleSwitch(
                Component.translatable(TRIGGER_POLARITY_TOOLTIP),
                offset.add(4, 14),
                state.trigger().risingSlope(),
                b -> setState.accept(state.withTriggerSlope(b))
        ).powered(moduleEnabled));
        final var enabled = state.moduleEnabled();
        components.add(ScopeButton.makeModuleEnable(
                offset.add(17, 3), enabled, () -> setState.accept(state.setEnabled(!enabled))
        ).powered(scopePowered));
        components.add(Range.makeLinear(
                Component.translatable(TRIGGER_LEVEL_TOOLTIP),
                offset.add(24, 12),
                0, 255, 1, 10, state.trigger().level(),
                i -> setState.accept(state.withTriggerLevel(i))
        ).powered(moduleEnabled));
        createChannelComponent(
                AnalogModule.TriggerChannel.LEFT, offset.add(0, 28), components, state, setState, moduleEnabled
        );
        createChannelComponent(
                AnalogModule.TriggerChannel.RIGHT, offset.add(24, 28), components, state, setState, moduleEnabled
        );
        return components;
    }

    private void createChannelComponent(
            AnalogModule.TriggerChannel channel,
            Vec2i baseOffset,
            List<PoweredComponent> out,
            State state,
            Consumer<State> setState,
            boolean powered
    ) {
        final var channelState = state.getChannel(channel);
        final var isEnabled = channelState.enabled();
        out.add(ScopeButton.makeChannelEnable(
                baseOffset.add(13, 2), isEnabled, () -> setState.accept(state.setChannelEnabled(channel, !isEnabled))
        ).powered(powered));
        out.add(ScopeButton.makeTriggerEnable(
                baseOffset.add(9, 2),
                state.trigger().source() == channel,
                () -> setState.accept(state.withTriggerChannel(channel))
        ).powered(powered));
        out.add(Range.makeExponential(
                Component.translatable(PER_DIV_TOOLTIP),
                baseOffset.add(2, 29),
                1, 255, 10, channelState.perDiv(),
                i -> setState.accept(state.setPerDiv(channel, i))
        ).powered(powered));
        out.add(Range.makeVerticalOffset(
                baseOffset.add(2, 8), channelState.zeroOffsetPixels(), i -> setState.accept(state.setOffset(channel, i))
        ).powered(powered));
        final Component bncTooltip;
        if (channelState.signal().isPresent()) {
            final var signal = channelState.signal().get();
            final var colorKey = "color.minecraft." + DyeColor.byId(signal.color()).getName();
            bncTooltip = Component.translatable(BNC_CONNECTED, Component.translatable(colorKey), signal.line());
        } else {
            bncTooltip = Component.translatable(BNC_OPEN);
        }
        out.add(new BNCConnector(
                baseOffset.add(5, 43),
                channelState.signal().orElse(null),
                bncTooltip,
                bsr -> setState.accept(state.setSignalSource(channel, Optional.ofNullable(bsr)))
        ).powered(powered));
    }

    @Override
    protected List<RectangleI> computeRelativeChannelAreas() {
        return List.of(new RectangleI(1, 29, 24, 88), new RectangleI(25, 29, 48, 88));
    }
}
