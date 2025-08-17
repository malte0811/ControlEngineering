package malte0811.controlengineering.gui.scope.components;

import malte0811.controlengineering.gui.scope.module.ClientModule.PoweredComponent;
import malte0811.controlengineering.util.math.RectangleI;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IScopeComponent {
    void render(GuiGraphics graphics);

    boolean click(double x, double y);

    RectangleI getArea();

    List<Component> getTooltip();

    boolean requiresPower();

    default PoweredComponent powered(boolean hasPower) {
        return new PoweredComponent(this, hasPower);
    }
}
