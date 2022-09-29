package malte0811.controlengineering.gui.scope.components;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.util.math.RectangleI;
import net.minecraft.network.chat.Component;

public interface IScopeComponent {
    void render(PoseStack transform);

    boolean click(double x, double y);

    RectangleI getArea();

    Component getTooltip();

    boolean requiresPower();
}
