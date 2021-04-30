package malte0811.controlengineering.gui.panel;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.client.render.target.DynamicRenderTarget;
import malte0811.controlengineering.client.render.target.TargetType;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Quaternion;
import org.lwjgl.opengl.GL11;

import java.util.function.Predicate;

public class GuiRenderTarget extends DynamicRenderTarget {
    public static final Quaternion VIEW_ROTATION = new Quaternion(-100, 0, 1, true);

    public GuiRenderTarget(Predicate<TargetType> doRender) {
        super(Util.make(() -> {
            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            return bufferbuilder;
        }), LightTexture.packLight(15, 15), OverlayTexture.NO_OVERLAY, doRender);
    }

    public static void renderSingleComponent(PanelComponentInstance<?, ?> comp, MatrixStack transform) {
        GuiRenderTarget target = new GuiRenderTarget($ -> true);
        ComponentRenderers.render(target, comp, transform);
        target.done();
    }

    public static void renderTopView(
            PanelComponentInstance<?, ?> comp, MatrixStack transform, double x, double y, float pixelSize
    ) {
        transform.push();
        transform.translate(x, y, 0);
        transform.scale(pixelSize, pixelSize, 1);
        setupTopView(transform);
        renderSingleComponent(comp, transform);
        transform.pop();
    }

    public static void setupTopView(MatrixStack transform) {
        transform.scale(1, 1, 0.01f);
        transform.translate(0, 0, 2);
        transform.rotate(VIEW_ROTATION);
    }

    public void done() {
        Tessellator.getInstance().draw();
    }
}
